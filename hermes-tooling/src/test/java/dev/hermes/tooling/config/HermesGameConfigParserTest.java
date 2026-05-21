package dev.hermes.tooling.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesGameConfigParserTest {

  @Test
  void parse_readsTitleAndScene(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("hermes.json");
    Files.writeString(
        file,
        "{\n  \"title\": \"My Game\",\n  \"scene\": \"scenes/custom.json\"\n}\n",
        StandardCharsets.UTF_8);

    HermesGameConfig config = HermesGameConfigParser.parse(file.toFile());

    assertEquals("My Game", config.getTitle());
    assertEquals("scenes/custom.json", config.getScene());
  }

  @Test
  void parse_emptyObjectUsesDefaults(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("hermes.json");
    Files.writeString(file, "{}", StandardCharsets.UTF_8);

    HermesGameConfig config = HermesGameConfigParser.parse(file.toFile());

    assertEquals("HermesGame", config.getTitle());
    assertEquals("scenes/main.json", config.getScene());
  }

  @Test
  void parse_missingFileThrows(@TempDir Path dir) {
    Path file = dir.resolve("missing.json");

    HermesConfigException error =
        assertThrows(HermesConfigException.class, () -> HermesGameConfigParser.parse(file.toFile()));

    assertTrue(error.getMessage().contains("hermes.json not found"));
  }

  @Test
  void parse_invalidJsonThrows(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("hermes.json");
    Files.writeString(file, "{ not json", StandardCharsets.UTF_8);

    HermesConfigException error =
        assertThrows(HermesConfigException.class, () -> HermesGameConfigParser.parse(file.toFile()));

    assertTrue(error.getMessage().contains("Invalid hermes.json"));
  }
}
