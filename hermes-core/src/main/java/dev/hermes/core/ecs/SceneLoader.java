package dev.hermes.core.ecs;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.core.HermesAssetPaths;
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
    FileHandle handle = HermesAssetPaths.internal(scenePath);
    if (!handle.exists()) {
      throw new SceneLoadException("Scene '" + scenePath + "': file not found");
    }
    String json = handle.readString(StandardCharsets.UTF_8.name());
    SceneParser.loadIntoWorld(scenePath, json, world, registry);
  }

  /** Loads scene JSON using world and registry from {@link SceneLoadContext}. */
  public static void load(String scenePath, SceneLoadContext ctx) {
    ComponentRegistry registry = ctx.registry();
    if (!(registry instanceof ComponentRegistryImpl)) {
      throw new IllegalStateException(
          "SceneLoader requires a ComponentRegistryImpl, got "
              + (registry == null ? "null" : registry.getClass().getName()));
    }
    load(scenePath, ctx.world(), (ComponentRegistryImpl) registry);
  }
}
