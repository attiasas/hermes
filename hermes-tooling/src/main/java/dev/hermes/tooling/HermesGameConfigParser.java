package dev.hermes.tooling;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class HermesGameConfigParser {

  private static final Set<String> KNOWN_KEYS = Set.of("title", "scene");
  private static final Gson GSON = new Gson();

  private HermesGameConfigParser() {}

  public static HermesGameConfig parse(File file) {
    if (!file.isFile()) {
      throw new HermesConfigException("hermes.json not found: " + file.getAbsolutePath());
    }
    try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      if (root == null) {
        throw new HermesConfigException("hermes.json is empty: " + file.getAbsolutePath());
      }
      warnUnknownKeys(file, root);
      HermesGameConfig config = new HermesGameConfig();
      if (root.has("title")) {
        config.setTitle(root.get("title").getAsString());
      }
      if (root.has("scene")) {
        config.setScene(root.get("scene").getAsString());
      }
      return config;
    } catch (JsonParseException e) {
      throw new HermesConfigException(
          "Invalid hermes.json at " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    } catch (IOException e) {
      throw new HermesConfigException("Failed to read hermes.json at " + file.getAbsolutePath(), e);
    }
  }

  private static void warnUnknownKeys(File file, JsonObject root) {
    Set<String> unknown = new HashSet<>();
    for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
      if (!KNOWN_KEYS.contains(entry.getKey())) {
        unknown.add(entry.getKey());
      }
    }
    if (!unknown.isEmpty()) {
      System.err.println(
          "Warning: hermes.json at "
              + file.getAbsolutePath()
              + " contains unknown keys (ignored): "
              + unknown);
    }
  }
}
