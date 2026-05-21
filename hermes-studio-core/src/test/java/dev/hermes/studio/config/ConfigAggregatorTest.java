package dev.hermes.studio.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.tooling.HermesGameConfig;
import dev.hermes.tooling.HermesGameConfigParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ConfigAggregatorTest {

  @Test
  void saveRoutesSceneToHermesJsonAndDebugToBuildGradle(@TempDir Path root) throws Exception {
    Path game = root.resolve("game");
    Files.createDirectories(game);
    Files.writeString(
        game.resolve("hermes.json"),
        """
        {
          "title": "T",
          "scene": "scenes/main.json"
        }
        """);
    Files.writeString(
        game.resolve("build.gradle"),
        """
        plugins { id 'dev.hermes' }
        version = '0.1.0'
        hermes {
          applicationClass = 'com.example.Game'
          assetsDirectory = 'src/main/resources/assets'
          debug = true
          platforms {
            desktop { width = 640 height = 480 }
            html { width = 800 height = 600 }
          }
        }
        """);
    Files.writeString(
        root.resolve("settings.gradle"),
        """
        rootProject.name = 'demo'
        hermes {
          platforms {
            desktop { enabled = true }
            html { enabled = true }
            android { enabled = false }
          }
        }
        """);

    ConfigAggregator agg = new ConfigAggregator();
    HermesProjectConfigView loaded = agg.load(root);
    HermesProjectConfigView edited =
        new HermesProjectConfigView(
            new HermesProjectConfigView.GameSection(
                "T", "scenes/other.json", loaded.game().sourceFile()),
            new HermesProjectConfigView.ProjectSection(
                loaded.project().applicationClass(),
                loaded.project().assetsDirectory(),
                false,
                loaded.project().version(),
                loaded.project().sourceFile()),
            loaded.platforms());
    agg.save(root, edited);

    HermesGameConfig gameConfig =
        HermesGameConfigParser.parse(root.resolve("game/hermes.json").toFile());
    assertEquals("scenes/other.json", gameConfig.getScene());
    assertTrue(Files.readString(root.resolve("game/build.gradle")).contains("debug = false"));
  }
}
