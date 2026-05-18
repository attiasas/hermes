package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesDesktopExportFixupTest {

  @Test
  void fixStagingDirectory_setsMacOsLauncherExecutable(@TempDir Path temp) throws Exception {
    Path app = temp.resolve("MyGame.app/Contents/MacOS");
    Files.createDirectories(app);
    Path launcher = app.resolve("MyGame");
    Files.writeString(launcher, "bin", StandardCharsets.UTF_8);
    assertFalse(Files.isExecutable(launcher));

    HermesDesktopExportFixup.fixStagingDirectory(temp.toFile());

    assertTrue(Files.isExecutable(launcher));
  }
}
