package dev.hermes.studio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ProjectServiceTest {

  @Test
  void opensProjectWithHermesJson(@TempDir Path dir) throws Exception {
    Path game = dir.resolve("game");
    Files.createDirectories(game);
    Files.writeString(game.resolve("hermes.json"), "{\"title\":\"T\",\"scene\":\"scenes/main.json\"}");
    Files.writeString(dir.resolve("settings.gradle"), "rootProject.name='x'");
    ProjectService svc = new ProjectService();
    HermesProject p = svc.open(dir);
    assertEquals(dir, p.root());
    assertTrue(p.hermesJson().toString().endsWith("game/hermes.json"));
  }
}
