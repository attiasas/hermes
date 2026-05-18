package dev.hermes.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

final class TemplateSupport {

  private TemplateSupport() {}

  static void materializeEmptyTemplate(Path targetDir, String projectName, String packageName, String engineVersion)
      throws IOException {
    if (Files.exists(targetDir)) {
      try (Stream<Path> entries = Files.list(targetDir)) {
        if (entries.findAny().isPresent()) {
          throw new IOException("Target directory is not empty: " + targetDir);
        }
      }
    }
    Files.createDirectories(targetDir);
    Path templateRoot = locateTemplateRoot();
    Map<String, String> tokens = buildTokens(projectName, packageName, engineVersion);
    copyAndSubstitute(templateRoot, targetDir, tokens);
    mergeEngineVersionsIntoGradleProperties(targetDir);
    makeGradleWrapperExecutable(targetDir);
  }

  private static void mergeEngineVersionsIntoGradleProperties(Path targetDir) throws IOException {
    java.io.File home = HermesHomeDetector.detect(targetDir);
    java.util.Properties versions = dev.hermes.tooling.HermesEngineVersions.resolveForNewProject(home);
    Path props = targetDir.resolve("gradle.properties");
    String existing = Files.readString(props, StandardCharsets.UTF_8);
    StringBuilder appended = new StringBuilder();
    for (String key : dev.hermes.tooling.HermesEngineVersions.GRADLE_PROPERTY_KEYS) {
      String value = versions.getProperty(key);
      if (value == null || value.isBlank() || existing.contains(key + "=")) {
        continue;
      }
      appended.append(key).append('=').append(value).append('\n');
    }
    if (appended.length() > 0) {
      Files.writeString(
          props,
          existing.endsWith("\n") ? existing + appended : existing + "\n" + appended,
          StandardCharsets.UTF_8);
    }
  }

  private static void makeGradleWrapperExecutable(Path projectDir) throws IOException {
    Path gradlew = projectDir.resolve("gradlew");
    if (!Files.isRegularFile(gradlew)) {
      return;
    }
    try {
      Set<PosixFilePermission> perms = Files.getPosixFilePermissions(gradlew);
      EnumSet<PosixFilePermission> executable =
          EnumSet.of(
              PosixFilePermission.OWNER_EXECUTE,
              PosixFilePermission.GROUP_EXECUTE,
              PosixFilePermission.OTHERS_EXECUTE);
      if (!perms.containsAll(executable)) {
        EnumSet<PosixFilePermission> updated = EnumSet.copyOf(perms);
        updated.addAll(executable);
        Files.setPosixFilePermissions(gradlew, updated);
      }
    } catch (UnsupportedOperationException ignored) {
      // Windows or FS without POSIX permissions
    }
  }

  private static Path locateTemplateRoot() throws IOException {
    Path fromFilesystem = findTemplateOnFilesystem();
    if (fromFilesystem != null) {
      return fromFilesystem;
    }
    try (InputStream marker =
        TemplateSupport.class.getResourceAsStream("/hermes-templates/empty/settings.gradle")) {
      if (marker == null) {
        throw new IOException(
            "Template hermes-templates/empty not found in CLI resources. Rebuild hermes-cli.");
      }
    }
    Path extracted = Files.createTempDirectory("hermes-template-empty");
    extractResourceTree("/hermes-templates/empty", extracted);
    return extracted;
  }

  private static void extractResourceTree(String resourcePrefix, Path targetDir) throws IOException {
    java.net.URL rootUrl = TemplateSupport.class.getResource(resourcePrefix);
    if (rootUrl == null) {
      throw new IOException("Missing resource prefix " + resourcePrefix);
    }
    if ("jar".equals(rootUrl.getProtocol())) {
      String jarPath = rootUrl.getPath();
      int bang = jarPath.indexOf('!');
      try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath.substring(5, bang))) {
        String prefix = resourcePrefix.startsWith("/") ? resourcePrefix.substring(1) : resourcePrefix;
        if (!prefix.endsWith("/")) {
          prefix = prefix + "/";
        }
        java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          java.util.jar.JarEntry entry = entries.nextElement();
          if (!entry.getName().startsWith(prefix) || entry.isDirectory()) {
            continue;
          }
          String relative = entry.getName().substring(prefix.length());
          Path dest = targetDir.resolve(relative);
          Files.createDirectories(dest.getParent());
          try (InputStream in = jar.getInputStream(entry)) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
          }
        }
      }
      return;
    }
    try {
      Path sourceDir = Path.of(rootUrl.toURI());
      copyAndSubstitute(sourceDir, targetDir, Map.of());
    } catch (java.net.URISyntaxException e) {
      throw new IOException("Invalid template resource URI: " + rootUrl, e);
    }
  }

  private static Path findTemplateOnFilesystem() {
    Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
    for (int i = 0; i < 8 && current != null; i++) {
      Path candidate = current.resolve("hermes-templates/empty");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
      current = current.getParent();
    }
    return null;
  }

  private static Map<String, String> buildTokens(String projectName, String packageName, String engineVersion) {
    String safeName = projectName.replaceAll("[^a-zA-Z0-9_-]", "");
    if (safeName.isBlank()) {
      safeName = "game";
    }
    String rootProjectName = safeName.toLowerCase(Locale.ROOT);
    String applicationClass = packageName + ".Game";
    String packageDir = packageName.replace('.', '/');
    Map<String, String> tokens = new HashMap<>();
    tokens.put("{{PROJECT_NAME}}", projectName);
    tokens.put("{{ROOT_PROJECT_NAME}}", rootProjectName);
    tokens.put("{{PACKAGE}}", packageName);
    tokens.put("{{package}}", packageName);
    tokens.put("{{APPLICATION_CLASS}}", applicationClass);
    tokens.put("{{ENGINE_VERSION}}", engineVersion);
    tokens.put("{{packageDir}}", packageDir);
    return tokens;
  }

  private static void copyAndSubstitute(Path source, Path target, Map<String, String> tokens) throws IOException {
    Files.walkFileTree(
        source,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path relative = source.relativize(dir);
            Path destDir = target.resolve(relative);
            if (relative.toString().contains("{{packageDir}}") && !tokens.isEmpty()) {
              String replaced = substitute(relative.toString(), tokens);
              destDir = target.resolve(replaced);
            }
            Files.createDirectories(destDir);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path relative = source.relativize(file);
            String relativeString = relative.toString();
            if (!tokens.isEmpty()) {
              relativeString = substitute(relativeString, tokens);
            }
            Path dest = target.resolve(relativeString);
            Files.createDirectories(dest.getParent());
            if (isTextFile(file)) {
              String content = Files.readString(file, StandardCharsets.UTF_8);
              if (!tokens.isEmpty()) {
                content = substitute(content, tokens);
              }
              Files.writeString(dest, content, StandardCharsets.UTF_8);
            } else {
              Files.copy(
                  file,
                  dest,
                  StandardCopyOption.REPLACE_EXISTING,
                  StandardCopyOption.COPY_ATTRIBUTES);
            }
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private static boolean isTextFile(Path file) {
    String path = file.toString().replace('\\', '/');
    if (path.contains("META-INF/services/")) {
      return true;
    }
    String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
    return name.endsWith(".gradle")
        || name.endsWith(".java")
        || name.endsWith(".json")
        || name.endsWith(".properties")
        || name.endsWith(".md")
        || name.endsWith(".txt");
  }

  private static String substitute(String input, Map<String, String> tokens) {
    String result = input;
    for (Map.Entry<String, String> entry : tokens.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
