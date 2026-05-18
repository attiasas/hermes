package dev.hermes.core.ecs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.ecs.World;
import java.nio.charset.StandardCharsets;

/** Loads scene JSON from libGDX internal file handles into a world. */
public final class SceneLoader {

  private SceneLoader() {}

  /** Loads scene JSON from a string (used by tests and {@link #load}). */
  public static void loadFromString(
      String scenePath, String json, World world, ComponentRegistryImpl registry) {
    SceneParser.loadIntoWorld(scenePath, json, world, registry);
  }

  public static void load(String scenePath, World world, ComponentRegistryImpl registry) {
    if (scenePath == null || scenePath.isBlank()) {
      return;
    }
    FileHandle handle = Gdx.files.internal(scenePath);
    if (!handle.exists()) {
      throw new SceneLoadException("Scene '" + scenePath + "': file not found");
    }
    String json = handle.readString(StandardCharsets.UTF_8.name());
    SceneParser.loadIntoWorld(scenePath, json, world, registry);
  }
}
