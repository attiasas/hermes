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

  private HermesPlatformSync() {}

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
    if (!isSynced(rootDir, moduleName)) {
      if (HermesHomeResolver.isHermesCheckout(hermesHome)) {
        copyLauncherFromHome(rootDir, hermesHome, moduleName, engineVersion);
      } else {
        extractLauncherFromPlugin(rootDir, moduleName, engineVersion);
      }
    }
    patchLauncherForMaven(launcherDir(rootDir, moduleName), engineVersion);
  }

  private static void patchLauncherForMaven(File launcherDir, String engineVersion) {
    File buildFile = new File(launcherDir, "build.gradle");
    if (!buildFile.isFile()) {
      return;
    }
    try {
      String content = Files.readString(buildFile.toPath());
      String coord = "dev.hermes:hermes-core:" + engineVersion;
      String patched =
          content
              .replace("implementation project(':hermes-core')", "implementation '" + coord + "'")
              .replace("implementation(project(':hermes-core'))", "implementation '" + coord + "'");
      if (!patched.equals(content)) {
        Files.writeString(buildFile.toPath(), patched);
      }
    } catch (IOException e) {
      throw new GradleException("Failed to patch " + buildFile.getAbsolutePath(), e);
    }
  }

  public static void syncIfNeeded(Settings settings, String moduleName, String engineVersion) {
    syncIfNeeded(settings.getRootDir(), moduleName, engineVersion, HermesHomeResolver.resolve(settings));
  }

  public static void syncAllEnabled(File rootDir, PlatformsExtension platforms, String engineVersion, File hermesHome) {
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
