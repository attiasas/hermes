package dev.hermes.gradle.platform;

import dev.hermes.gradle.dsl.HermesSettingsExtension;
import dev.hermes.gradle.dsl.SettingsPlatformsExtension;
import dev.hermes.gradle.internal.HermesHomeGradle;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.gradle.api.GradleException;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logging;

/** Syncs launcher Gradle projects into {@code .hermes/platforms/} when not in the monorepo. */
public final class HermesPlatformSync {

  static final String PLATFORMS_DIR = ".hermes/platforms";
  static final String VERSION_FILE = ".hermes/version";

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
    if (!isSynced(rootDir, moduleName) || engineVersionOutdated(rootDir, engineVersion)) {
      populateLauncherSources(rootDir, moduleName, hermesHome);
    }
    renderBuildGradle(rootDir, moduleName, engineVersion);
    writeVersionStamp(rootDir, engineVersion);
  }

  public static void syncIfNeeded(Settings settings, String moduleName, String engineVersion) {
    syncIfNeeded(settings.getRootDir(), moduleName, engineVersion, HermesHomeGradle.resolve(settings));
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

  /** Re-extracts all enabled launcher sources (for {@code hermesSyncPlatforms}). */
  public static void syncAllEnabledForce(
      File rootDir, SettingsPlatformsExtension platforms, String engineVersion, File hermesHome) {
    if (platforms.getDesktop().isEnabled()) {
      populateLauncherSources(rootDir, "hermes-launcher-desktop", hermesHome);
      renderBuildGradle(rootDir, "hermes-launcher-desktop", engineVersion);
    }
    if (platforms.getHtml().isEnabled()) {
      populateLauncherSources(rootDir, "hermes-launcher-html", hermesHome);
      renderBuildGradle(rootDir, "hermes-launcher-html", engineVersion);
    }
    if (platforms.getAndroid().isEnabled()) {
      populateLauncherSources(rootDir, "hermes-launcher-android", hermesHome);
      renderBuildGradle(rootDir, "hermes-launcher-android", engineVersion);
    }
    writeVersionStamp(rootDir, engineVersion);
  }

  public static void syncAllEnabled(Settings settings, HermesSettingsExtension extension, String engineVersion) {
    syncAllEnabled(
        settings.getRootDir(),
        extension.getPlatforms(),
        engineVersion,
        HermesHomeGradle.resolve(settings));
  }

  private static void renderBuildGradle(File rootDir, String moduleName, String engineVersion) {
    File launcherDir = launcherDir(rootDir, moduleName);
    PlatformSyncContext context = PlatformSyncContext.forStandalone(moduleName, engineVersion, rootDir);
    String content = PlatformTemplateRenderer.render(moduleName, context);
    try {
      Files.createDirectories(launcherDir.toPath());
      Files.writeString(new File(launcherDir, "build.gradle").toPath(), content);
    } catch (IOException e) {
      throw new GradleException(
          "Failed to write build.gradle for " + moduleName + " at " + launcherDir.getAbsolutePath(), e);
    }
  }

  static boolean engineVersionOutdated(File rootDir, String engineVersion) {
    File versionFile = new File(rootDir, VERSION_FILE);
    if (!versionFile.isFile()) {
      return true;
    }
    try {
      String stamped = Files.readString(versionFile.toPath()).trim();
      return !stamped.equals(engineVersion.trim());
    } catch (IOException e) {
      return true;
    }
  }

  private static void populateLauncherSources(File rootDir, String moduleName, File hermesHome) {
    File target = launcherDir(rootDir, moduleName);
    try {
      if (target.exists()) {
        deleteRecursive(target.toPath());
      }
      Files.createDirectories(target.toPath());
      if (HermesHomeGradle.isHermesCheckout(hermesHome)) {
        File source = new File(hermesHome, moduleName);
        if (!source.isDirectory()) {
          throw new GradleException(
              "HERMES_HOME is set to "
                  + hermesHome.getAbsolutePath()
                  + " but "
                  + moduleName
                  + " was not found.");
        }
        copyLauncherSources(source.toPath(), target.toPath());
        Logging.getLogger(HermesPlatformSync.class)
            .lifecycle("Hermes: synced {} from HERMES_HOME to {}", moduleName, target.getAbsolutePath());
      } else {
        if (HermesPlatformSync.class.getResource("/hermes-platforms/" + moduleName) == null) {
          throw new GradleException(
              "Launcher "
                  + moduleName
                  + " is not in this build and could not be loaded from the Hermes plugin."
                  + " Set HERMES_HOME to a Hermes checkout or run from the engine monorepo.");
        }
        extractResourceTree(moduleName, target.toPath());
        Logging.getLogger(HermesPlatformSync.class)
            .lifecycle("Hermes: extracted {} into {}", moduleName, target.getAbsolutePath());
      }
    } catch (IOException e) {
      throw new GradleException("Failed to sync launcher sources for " + moduleName, e);
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
          if ("build.gradle".equals(relative)) {
            continue;
          }
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
      copyLauncherSources(sourceDir, targetDir);
    } catch (java.net.URISyntaxException e) {
      throw new IOException("Invalid platform resource URI: " + location, e);
    }
  }

  static void copyLauncherSources(Path source, Path target) throws IOException {
    copyTree(source, target, true);
  }

  static void copyTree(Path source, Path target) throws IOException {
    copyTree(source, target, false);
  }

  private static void copyTree(Path source, Path target, boolean skipBuildGradle) throws IOException {
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
            if (skipBuildGradle && "build.gradle".equals(file.getFileName().toString())) {
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
