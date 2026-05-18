package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

class HermesDesktopExportFixupTest {

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void fixStagingDirectory_setsMacOsLauncherExecutable_windows(@TempDir Path temp) throws Exception {
    Path app = temp.resolve("MyGame.app/Contents/MacOS");
    Files.createDirectories(app);
    Path launcher = app.resolve("MyGame");
    Files.writeString(launcher, "bin", StandardCharsets.UTF_8);

    HermesDesktopExportFixup.fixStagingDirectory(temp.toFile());

    assertTrue(launcher.toFile().canExecute());
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC})
  void fixStagingDirectory_setsMacOsLauncherExecutable_posix(@TempDir Path temp) throws Exception {
    Path app = temp.resolve("MyGame.app/Contents/MacOS");
    Files.createDirectories(app);
    Path launcher = app.resolve("MyGame");
    Files.writeString(launcher, "bin", StandardCharsets.UTF_8);
    Set<PosixFilePermission> noExecute = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
    Files.setPosixFilePermissions(launcher, noExecute);
    assertFalse(Files.isExecutable(launcher));

    HermesDesktopExportFixup.fixStagingDirectory(temp.toFile());

    assertTrue(Files.isExecutable(launcher));
  }
}
