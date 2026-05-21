package dev.hermes.tooling.config;

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

  private static final Set<String> KNOWN_KEYS = Set.of("title", "scene", "renderPipeline");
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
      if (!root.has("renderPipeline")) {
        throw new HermesConfigException(
            "hermes.json must include \"renderPipeline\" at " + file.getAbsolutePath());
      }
      String renderPipeline = root.get("renderPipeline").getAsString();
      if (renderPipeline.isBlank()) {
        throw new HermesConfigException(
            "hermes.json \"renderPipeline\" must be non-empty at " + file.getAbsolutePath());
      }
      config.setRenderPipeline(renderPipeline);
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
