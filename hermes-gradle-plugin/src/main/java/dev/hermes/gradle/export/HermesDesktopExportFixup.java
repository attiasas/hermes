package dev.hermes.gradle.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.gradle.api.GradleException;

/** Ensures desktop export bundles are runnable after unzip (executable bits on launchers). */
public final class HermesDesktopExportFixup {

  private HermesDesktopExportFixup() {}

  public static void fixStagingDirectory(File stagingDir) {
    if (stagingDir == null || !stagingDir.isDirectory()) {
      return;
    }
    try {
      Files.walkFileTree(
          stagingDir.toPath(),
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
              if (dir.getFileName() != null && dir.getFileName().toString().endsWith(".app")) {
                fixMacOsApp(dir.toFile());
                return FileVisitResult.SKIP_SUBTREE;
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              if (isLikelyLauncherBinary(file)) {
                file.toFile().setExecutable(true, false);
              }
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      throw new GradleException("Failed to fix desktop export staging at " + stagingDir, e);
    }
  }

  private static void fixMacOsApp(File appBundle) {
    File macOsDir = new File(appBundle, "Contents/MacOS");
    if (!macOsDir.isDirectory()) {
      return;
    }
    File[] files = macOsDir.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.isFile() && !file.getName().endsWith(".jar")) {
        file.setExecutable(true, false);
      }
    }
  }

  private static boolean isLikelyLauncherBinary(Path file) {
    if (!Files.isRegularFile(file)) {
      return false;
    }
    String name = file.getFileName().toString();
    if (name.endsWith(".jar") || name.endsWith(".png") || name.endsWith(".json")) {
      return false;
    }
    String path = file.toString().replace('\\', '/');
    if (path.contains("/runtime/") || path.contains("/legal/") || path.contains("/conf/")) {
      return false;
    }
    return path.contains(".app/Contents/MacOS/")
        || (!path.contains("/") && !name.contains("."))
        || path.contains("/bin/" + name);
  }
}
