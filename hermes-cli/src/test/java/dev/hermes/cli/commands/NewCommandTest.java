package dev.hermes.cli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.cli.HermesCli;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class NewCommandTest {

  @Test
  void new_createsEmptyProject(@TempDir Path parent) {
    Path target = parent.resolve("my-game");

    int exit =
        new CommandLine(new HermesCli())
            .execute(
                "new",
                target.toString(),
                "--name",
                "My Game",
                "--package",
                "dev.hermes.mygame",
                "--platforms",
                "desktop");

    assertEquals(0, exit);
    assertTrue(Files.isRegularFile(target.resolve("settings.gradle")));
    assertTrue(Files.isRegularFile(target.resolve("game/build.gradle")));
    assertTrue(Files.isRegularFile(target.resolve("game/src/main/java/dev/hermes/mygame/Game.java")));
  }

  @Test
  void new_unknownTemplateFails(@TempDir Path parent) {
    Path target = parent.resolve("bad-template");

    int exit = new CommandLine(new HermesCli()).execute("new", target.toString(), "--template", "nope");

    assertEquals(2, exit);
  }

  @Test
  void new_substitutesPackageInSpi(@TempDir Path parent) throws Exception {
    Path target = parent.resolve("spi-game");

    int exit =
        new CommandLine(new HermesCli())
            .execute("new", target.toString(), "--package", "dev.hermes.spigame");

    assertEquals(0, exit);
    String spi =
        Files.readString(
            target.resolve(
                "game/src/main/resources/META-INF/services/dev.hermes.api.ecs.ComponentRegistration"),
            StandardCharsets.UTF_8);
    assertTrue(spi.contains("dev.hermes.spigame.PulseMarkerRegistration"));
  }
}
