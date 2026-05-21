package dev.hermes.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudioCommandTest {

  @Test
  void resolveEngineRoot_findsCheckoutByWalkingUp(@TempDir Path root) throws Exception {
    Path engine = root.resolve("hermes");
    Files.createDirectories(engine.resolve("hermes-api"));
    Files.createDirectories(engine.resolve("hermes-core"));
    Path game = engine.resolve("game");
    Files.createDirectories(game);

    assertEquals(engine, StudioCommand.resolveEngineRoot(game));
  }

  @Test
  void resolveStudioJar_prefersBundledFatJar(@TempDir Path engine) throws Exception {
    Files.createDirectories(engine.resolve("hermes-api"));
    Files.createDirectories(engine.resolve("hermes-core"));
    Path studioDir = engine.resolve("studio");
    Files.createDirectories(studioDir);
    Path fatJar = studioDir.resolve(StudioCommand.STUDIO_JAR_NAME);
    Files.writeString(fatJar, "fat");

    assertEquals(fatJar, StudioCommand.resolveStudioJar(engine));
  }

  @Test
  void resolveEngineRoot_returnsNullOutsideCheckout(@TempDir Path dir) {
    assertNull(StudioCommand.resolveEngineRoot(dir));
  }

  @Test
  void resolveStudioJar_returnsNullWhenMissing(@TempDir Path engine) throws Exception {
    Files.createDirectories(engine.resolve("hermes-api"));
    Files.createDirectories(engine.resolve("hermes-core"));
    assertNull(StudioCommand.resolveStudioJar(engine));
  }

  @Test
  void resolveEngineRoot_fromProjectDirWhenCheckout(@TempDir Path engine) throws Exception {
    Files.createDirectories(engine.resolve("hermes-api"));
    Files.createDirectories(engine.resolve("hermes-core"));
    assertNotNull(StudioCommand.resolveEngineRoot(engine));
    assertEquals(engine, StudioCommand.resolveEngineRoot(engine));
  }
}
