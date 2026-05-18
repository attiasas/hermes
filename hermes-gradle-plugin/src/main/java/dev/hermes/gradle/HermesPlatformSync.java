package dev.hermes.gradle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gradle.api.GradleException;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logging;

/** Syncs launcher Gradle projects into {@code .hermes/platforms/} when not in the monorepo. */
public final class HermesPlatformSync {

  static final String PLATFORMS_DIR = ".hermes/platforms";
  static final String VERSION_FILE = ".hermes/version";

  private static final String[] LAUNCHER_MODULES = {
    "hermes-launcher-desktop", "hermes-launcher-html", "hermes-launcher-android"
  };

  private static final String DESKTOP_CONSTRUO_JDK_ROOT_BLOCK =
      """
      // Construo defaults jlink to java.home; use the downloaded JDK so versions match target jmods.
      tasks.withType(CreateRuntimeImageTask).configureEach { CreateRuntimeImageTask task ->
        String target = task.name.substring('createRuntimeImage'.length())
        String targetKey = target.substring(0, 1).toLowerCase(Locale.ROOT) + target.substring(1)
        task.dependsOn("downloadJdk${target}", "unzipJdk${target}")
        task.jdkRoot.set(
            tasks.named("unzipJdk${target}").map {
              File dir = layout.buildDirectory.dir("construo/jdk/${targetKey}").get().asFile
              File jlink =
                  fileTree(dir).matching { include '**/bin/jlink' }.files.find { it?.isFile() }
              if (jlink == null) {
                throw new GradleException("No jlink under ${dir} (run unzipJdk${target} first)")
              }
              layout.projectDirectory.dir(jlink.parentFile.parentFile.absolutePath)
            })
      }
      """;

  private HermesPlatformSync() {}

  /** Patches use LF-based matching; normalize CRLF from Windows checkouts and JAR extraction. */
  private static String normalizeNewlines(String content) {
    return content.replace("\r\n", "\n").replace('\r', '\n');
  }

  private static String readBuildFileQuietly(File buildFile) {
    try {
      return readBuildFile(buildFile);
    } catch (IOException e) {
      return "";
    }
  }

  private static boolean isCorruptedDesktopBuild(String content) {
    if (content == null || content.isBlank()) {
      return false;
    }
    int construoApply = countOccurrences(content, "apply plugin: 'io.github.fourlastor.construo'");
    return construoApply > 1
        || content.contains("architecture.set(Target.Architecture")
        && content.indexOf("architecture.set(Target.Architecture") < content.indexOf("construo {");
  }

  private static int countOccurrences(String haystack, String needle) {
    int count = 0;
    int index = 0;
    while ((index = haystack.indexOf(needle, index)) >= 0) {
      count++;
      index += needle.length();
    }
    return count;
  }

  private static void deleteDirectoryQuietly(File dir) {
    if (!dir.exists()) {
      return;
    }
    try {
      Files.walkFileTree(
          dir.toPath(),
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              Files.deleteIfExists(file);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
              Files.deleteIfExists(directory);
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      Logging.getLogger(HermesPlatformSync.class)
          .warn("Failed to delete corrupted launcher dir {}: {}", dir, e.getMessage());
    }
  }

  private static void writeBuildFile(File buildFile, String content) throws IOException {
    Files.writeString(buildFile.toPath(), content);
  }

  private static String readBuildFile(File buildFile) throws IOException {
    return normalizeNewlines(Files.readString(buildFile.toPath()));
  }

  public static File platformRoot(File rootDir) {
    return new File(rootDir, PLATFORMS_DIR);
  }

  public static File launcherDir(File rootDir, String moduleName) {
    return new File(platformRoot(rootDir), moduleName);
  }

  public static File platformRoot(Settings settings) {
    return platformRoot(settings.getRootDir());
  }

  public static File launcherDir(Settings settings, String moduleName) {
    return launcherDir(settings.getRootDir(), moduleName);
  }

  public static boolean isSynced(File rootDir, String moduleName) {
    File dir = launcherDir(rootDir, moduleName);
    return dir.isDirectory() && new File(dir, "build.gradle").isFile();
  }

  public static boolean isSynced(Settings settings, String moduleName) {
    return isSynced(settings.getRootDir(), moduleName);
  }

  public static void syncIfNeeded(File rootDir, String moduleName, String engineVersion, File hermesHome) {
    if ("hermes-launcher-desktop".equals(moduleName)) {
      File desktopDir = launcherDir(rootDir, moduleName);
      File buildFile = new File(desktopDir, "build.gradle");
      if (buildFile.isFile() && isCorruptedDesktopBuild(readBuildFileQuietly(buildFile))) {
        deleteDirectoryQuietly(desktopDir);
      }
    }
    if (!isSynced(rootDir, moduleName)) {
      if (HermesHomeResolver.isHermesCheckout(hermesHome)) {
        copyLauncherFromHome(rootDir, hermesHome, moduleName, engineVersion);
      } else {
        extractLauncherFromPlugin(rootDir, moduleName, engineVersion);
      }
    }
    File launcherDir = launcherDir(rootDir, moduleName);
    patchLauncherForMaven(launcherDir, engineVersion);
    patchLauncherJava11(launcherDir);
    patchDesktopConstruoToolchain(launcherDir);
    patchDesktopConstruoJdkRoot(launcherDir);
    patchLauncherForStandaloneGame(launcherDir);
    patchAndroidPluginDeclaration(rootDir, launcherDir);
    stripAndroidProjectRepositories(launcherDir);
    stripNativeAccessFromLauncherBuild(launcherDir);
  }

  /** Project-level repos override {@code dependencyResolutionManagement}; use root repos for gdx/hermes. */
  private static void stripAndroidProjectRepositories(File launcherDir) {
    if (!"hermes-launcher-android".equals(launcherDir.getName())) {
      return;
    }
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String content = readBuildFile(buildFile);
      String patched =
          content
              .replace("\nrepositories {\n  google()\n}\n", "\n")
              .replace("\nrepositories {\n    google()\n}\n", "\n");
      if (!patched.equals(content)) {
        writeBuildFile(buildFile, patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to strip project repositories from " + buildFile.getAbsolutePath(), e);
    }
  }

  private static void patchAndroidPluginDeclaration(File rootDir, File launcherDir) {
    if (!"hermes-launcher-android".equals(launcherDir.getName())) {
      return;
    }
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String agpVersion = resolveAndroidGradlePluginVersion(rootDir);
      String androidHeader = androidBuildscriptBlock(agpVersion);
      String content = readBuildFile(buildFile);
      if (content.startsWith("buildscript {") && content.contains("com.android.tools.build:gradle")) {
        return;
      }
      String patched =
          content.replaceFirst(
              "(?s)^plugins \\{[^}]*com\\.android\\.application[^}]*\\}\\s*",
              androidHeader + "\n");
      if (patched.equals(content)) {
        patched =
            content.replaceFirst(
                "(?m)^apply plugin: ['\"]com\\.android\\.application['\"]\\s*\\n+",
                androidHeader + "\n");
      }
      if (!patched.equals(content)) {
        writeBuildFile(buildFile, patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to patch Android plugin declaration in " + buildFile.getAbsolutePath(), e);
    }
  }

  private static String androidBuildscriptBlock(String agpVersion) {
    return """
        buildscript {
          repositories {
            google()
            mavenCentral()
          }
          dependencies {
            classpath 'com.android.tools.build:gradle:%s'
          }
        }
        apply plugin: 'com.android.application'
        """
        .formatted(agpVersion)
        .stripLeading();
  }

  private static String resolveAndroidGradlePluginVersion(File rootDir) {
    File propsFile = new File(rootDir, "gradle.properties");
    if (propsFile.isFile()) {
      try {
        java.util.Properties props = new java.util.Properties();
        try (java.io.InputStream in = Files.newInputStream(propsFile.toPath())) {
          props.load(in);
        }
        String version = props.getProperty(HermesAndroidGradlePlugin.VERSION_PROPERTY);
        if (version != null && !version.isBlank()) {
          return version.trim();
        }
      } catch (IOException ignored) {
        // fall through
      }
    }
    return HermesAndroidGradlePlugin.DEFAULT_VERSION;
  }

  private static void stripNativeAccessFromLauncherBuild(File launcherDir) {
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String content = readBuildFile(buildFile);
      String patched = stripNativeAccessJvmArg(content);
      if (!patched.equals(content)) {
        writeBuildFile(buildFile, patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to strip JVM args from " + buildFile.getAbsolutePath(), e);
    }
  }

  private static void patchLauncherForMaven(File launcherDir, String engineVersion) {
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String content = readBuildFile(buildFile);
      String coord = "dev.hermes:hermes-core:" + engineVersion;
      String patched =
          content
              .replace("implementation project(':hermes-core')", "implementation '" + coord + "'")
              .replace("implementation(project(':hermes-core'))", "implementation '" + coord + "'");
      if (!patched.equals(content)) {
        writeBuildFile(buildFile, patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to patch " + buildFile.getAbsolutePath(), e);
    }
  }

  private static void patchLauncherForStandaloneGame(File launcherDir) {
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String content = readBuildFile(buildFile);
      String patched = content;
      if (content.contains("implementation project(':game')")) {
        patched =
            patched
                .replace("implementation project(':game')", "compileOnly project(':game')")
                .replace("implementation(project(':game'))", "compileOnly(project(':game'))");
      }
      if (!patched.equals(content)) {
        writeBuildFile(buildFile, patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to patch standalone HTML launcher " + buildFile.getAbsolutePath(), e);
    }
  }

  private static void patchLauncherJava11(File launcherDir) {
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String content = readBuildFile(buildFile);
      String patched = stripNativeAccessJvmArg(content);
      boolean hasToolchain =
          patched.contains("languageVersion")
              && (patched.contains("JavaLanguageVersion.of(11)")
                  || patched.contains("JavaLanguageVersion.of(17)"));
      if (!hasToolchain) {
        String toolchainBlock =
            """
            java {
              toolchain {
                languageVersion = JavaLanguageVersion.of(11)
              }
            }

            """;
        if (patched.contains("java {")) {
          patched =
              patched.replaceFirst(
                  "java \\{",
                  "java {\n  toolchain {\n    languageVersion = JavaLanguageVersion.of(11)\n  }");
        } else {
          patched = patched.replaceFirst("(?m)^plugins \\{[^}]+}", "$0\n\n" + toolchainBlock.trim());
        }
      }
      if (!patched.equals(content)) {
        writeBuildFile(buildFile, patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to patch Java toolchain in " + buildFile.getAbsolutePath(), e);
    }
  }

  /**
   * Resolves Construo {@code jdkRoot} at execution time (after {@code unzipJdk*}) so configuration
   * does not fail before the JDK is downloaded.
   */
  private static void patchDesktopConstruoJdkRoot(File launcherDir) {
    if (!"hermes-launcher-desktop".equals(launcherDir.getName())) {
      return;
    }
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String content = readBuildFile(buildFile);
      if (!content.contains("CreateRuntimeImageTask")) {
        return;
      }
      if (content.contains("tasks.named(\"unzipJdk${target}\").map {")
          && content.contains("task.jdkRoot.set(")) {
        return;
      }
      String patched = content;
      if (patched.contains("task.doFirst {")
          && patched.contains("task.jdkRoot.set(layout.projectDirectory.dir(jlink")) {
        patched =
            Pattern.compile("(?s)// Construo defaults jlink to java\\.home;.*?\\n\\s*\\}\\s*")
                .matcher(patched)
                .replaceFirst(Matcher.quoteReplacement(DESKTOP_CONSTRUO_JDK_ROOT_BLOCK.trim()) + "\n\n");
      }
      if (patched.contains("layout.buildDirectory.dir(\"construo/jdk/${targetKey}\").map { dir ->")) {
        patched =
            patched.replace(
                "task.dependsOn(\"unzipJdk${target}\")",
                "task.dependsOn(\"downloadJdk${target}\", \"unzipJdk${target}\")");
        patched =
            Pattern.compile(
                    "(?s)task\\.jdkRoot\\.set\\(\\s*layout\\.buildDirectory\\.dir\\(\"construo/jdk/\\$\\{targetKey\\}\"\\)\\.map \\{ dir ->.*?\\}\\s*\\)")
                .matcher(patched)
                .replaceFirst(
                    Matcher.quoteReplacement(
                        """
                        task.jdkRoot.set(
                            tasks.named("unzipJdk${target}").map {
                              File dir = layout.buildDirectory.dir("construo/jdk/${targetKey}").get().asFile
                              File jlink =
                                  fileTree(dir).matching { include '**/bin/jlink' }.files.find { it?.isFile() }
                              if (jlink == null) {
                                throw new GradleException("No jlink under ${dir} (run unzipJdk${target} first)")
                              }
                              layout.projectDirectory.dir(jlink.parentFile.parentFile.absolutePath)
                            })"""
                            .stripLeading()));
      } else if (patched.contains("CreateRuntimeImageTask")
          && !patched.contains("tasks.named(\"unzipJdk${target}\").map {")) {
        Pattern construoJdkBlock =
            Pattern.compile("(?s)// Construo defaults jlink to java\\.home;.*?\\n\\s*\\}\\s*");
        if (construoJdkBlock.matcher(patched).find()) {
          patched =
              construoJdkBlock
                  .matcher(patched)
                  .replaceFirst(Matcher.quoteReplacement(DESKTOP_CONSTRUO_JDK_ROOT_BLOCK.trim()) + "\n\n");
        } else {
          patched = patched.trim() + "\n\n" + DESKTOP_CONSTRUO_JDK_ROOT_BLOCK + "\n";
        }
      }
      if (!patched.equals(content)) {
        writeBuildFile(buildFile, patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to patch Construo jdkRoot in " + buildFile.getAbsolutePath(), e);
    }
  }

  /** Construo jlink must use JDK 17 to match downloaded runtime JDKs; game bytecode stays Java 11. */
  private static void patchDesktopConstruoToolchain(File launcherDir) {
    if (!"hermes-launcher-desktop".equals(launcherDir.getName())) {
      return;
    }
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String content = readBuildFile(buildFile);
      String patched = content;
      if (patched.contains("JavaLanguageVersion.of(11)")) {
        patched = patched.replace("JavaLanguageVersion.of(11)", "JavaLanguageVersion.of(17)");
      }
      if (!patched.contains("options.release = 11")) {
        String releaseBlock =
            """
            tasks.withType(JavaCompile).configureEach {
              options.release = 11
            }

            """;
        patched = patched.replace("apply plugin: 'io.github.fourlastor.construo'", releaseBlock + "apply plugin: 'io.github.fourlastor.construo'");
        if (patched.equals(content)) {
          patched = patched.replaceFirst("(?m)^plugins \\{[^}]+}", "$0\n\n" + releaseBlock.trim());
        }
      }
      if (!patched.equals(content)) {
        writeBuildFile(buildFile, patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to patch Construo JDK toolchain in " + buildFile.getAbsolutePath(), e);
    }
  }

  private static String stripNativeAccessJvmArg(String content) {
    return content
        .replace("  jvmArgs('--enable-native-access=ALL-UNNAMED')\n", "")
        .replace("  jvmArgs(\"--enable-native-access=ALL-UNNAMED\")\n", "")
        .replace("\n  jvmArgs('--enable-native-access=ALL-UNNAMED')", "");
  }

  public static void syncIfNeeded(Settings settings, String moduleName, String engineVersion) {
    syncIfNeeded(settings.getRootDir(), moduleName, engineVersion, HermesHomeResolver.resolve(settings));
  }

  public static void syncAllEnabled(
      File rootDir, SettingsPlatformsExtension platforms, String engineVersion, File hermesHome) {
    if (platforms.getDesktop().isEnabled()) {
      syncIfNeeded(rootDir, "hermes-launcher-desktop", engineVersion, hermesHome);
    }
    if (platforms.getHtml().isEnabled()) {
      syncIfNeeded(rootDir, "hermes-launcher-html", engineVersion, hermesHome);
    }
    if (platforms.getAndroid().isEnabled()) {
      syncIfNeeded(rootDir, "hermes-launcher-android", engineVersion, hermesHome);
    }
    writeVersionStamp(rootDir, engineVersion);
  }

  public static void syncAllEnabled(Settings settings, HermesSettingsExtension extension, String engineVersion) {
    syncAllEnabled(
        settings.getRootDir(),
        extension.getPlatforms(),
        engineVersion,
        HermesHomeResolver.resolve(settings));
  }

  private static void copyLauncherFromHome(
      File rootDir, File hermesHome, String moduleName, String engineVersion) {
    File source = new File(hermesHome, moduleName);
    if (!source.isDirectory()) {
      throw new GradleException(
          "HERMES_HOME is set to "
              + hermesHome.getAbsolutePath()
              + " but "
              + moduleName
              + " was not found.");
    }
    File target = launcherDir(rootDir, moduleName);
    try {
      copyTree(source.toPath(), target.toPath());
      writeVersionStamp(rootDir, engineVersion);
      Logging.getLogger(HermesPlatformSync.class)
          .lifecycle("Hermes: synced {} from HERMES_HOME to {}", moduleName, target.getAbsolutePath());
    } catch (IOException e) {
      throw new GradleException("Failed to sync " + moduleName + " from HERMES_HOME", e);
    }
  }

  private static void extractLauncherFromPlugin(File rootDir, String moduleName, String engineVersion) {
    URL resource = HermesPlatformSync.class.getResource("/hermes-platforms/" + moduleName + "/build.gradle");
    if (resource == null) {
      throw new GradleException(
          "Launcher "
              + moduleName
              + " is not in this build and could not be loaded from the Hermes plugin."
              + " Set HERMES_HOME to a Hermes checkout or run from the engine monorepo.");
    }
    File target = launcherDir(rootDir, moduleName);
    try {
      if (target.exists()) {
        deleteRecursive(target.toPath());
      }
      Files.createDirectories(target.toPath());
      extractResourceTree(moduleName, target.toPath());
      writeVersionStamp(rootDir, engineVersion);
      Logging.getLogger(HermesPlatformSync.class)
          .lifecycle("Hermes: extracted {} into {}", moduleName, target.getAbsolutePath());
    } catch (IOException e) {
      throw new GradleException("Failed to extract " + moduleName + " platform stub", e);
    }
  }

  private static void extractResourceTree(String moduleName, Path targetDir) throws IOException {
    String prefix = "hermes-platforms/" + moduleName + "/";
    URL location = HermesPlatformSync.class.getResource("/hermes-platforms/" + moduleName);
    if (location == null) {
      throw new IOException("Missing bundled platform " + moduleName);
    }
    if ("jar".equals(location.getProtocol())) {
      String jarPath = location.getPath();
      int bang = jarPath.indexOf('!');
      String jarFile = jarPath.substring(5, bang);
      try (JarFile jar = new JarFile(jarFile)) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
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
      Path sourceDir = Path.of(location.toURI());
      copyTree(sourceDir, targetDir);
    } catch (java.net.URISyntaxException e) {
      throw new IOException("Invalid platform resource URI: " + location, e);
    }
  }

  private static void writeVersionStamp(File rootDir, String engineVersion) {
    try {
      File versionFile = new File(rootDir, VERSION_FILE);
      File parent = versionFile.getParentFile();
      if (parent != null && !parent.exists()) {
        Files.createDirectories(parent.toPath());
      }
      Files.writeString(versionFile.toPath(), engineVersion.trim() + "\n");
    } catch (IOException e) {
      throw new GradleException("Failed to write " + VERSION_FILE, e);
    }
  }

  static void copyTree(Path source, Path target) throws IOException {
    Files.walkFileTree(
        source,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
            if (shouldSkip(dir)) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            Path relative = source.relativize(dir);
            Path dest = target.resolve(relative);
            Files.createDirectories(dest);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (shouldSkip(file)) {
              return FileVisitResult.CONTINUE;
            }
            Path relative = source.relativize(file);
            Path dest = target.resolve(relative);
            Files.createDirectories(dest.getParent());
            Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private static void deleteRecursive(Path root) throws IOException {
    if (!Files.exists(root)) {
      return;
    }
    Files.walkFileTree(
        root,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private static boolean shouldSkip(Path path) {
    String name = path.getFileName().toString();
    if (name.equals("build") || name.equals(".gradle") || name.equals("bin")) {
      return true;
    }
    return name.startsWith(".") && !name.equals(".gitignore");
  }
}
