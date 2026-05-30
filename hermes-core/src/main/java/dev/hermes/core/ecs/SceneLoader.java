package dev.hermes.core.ecs;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;

/**
 * Loads scene JSON from libGDX internal file handles into an entity store.
 */
public final class SceneLoader {

    private static final Logger log = Logs.get(SceneLoader.class);

    private SceneLoader() {
    }

    /**
     * Loads scene JSON from a string (used by tests and {@link #load}).
     */
    public static SceneLoadMetadata loadFromString(
            String scenePath, String json, EntityStore entities, ComponentRegistryImpl registry) {
        return SceneParser.loadIntoEntities(scenePath, json, entities, registry);
    }

    public static SceneLoadMetadata load(String scenePath, EntityStore entities, ComponentRegistryImpl registry) {
        if (scenePath == null || scenePath.isBlank()) {
            return SceneLoadMetadata.empty();
        }
        FileHandle handle = HermesAssetPaths.internal(scenePath);
        if (!handle.exists()) {
            throw new SceneLoadException("Scene '" + scenePath + "': file not found");
        }
        String json = handle.readString(StandardCharsets.UTF_8.name());
        log.debug("Loading scene: " + scenePath + " with JSON: " + json);
        return SceneParser.loadIntoEntities(scenePath, json, entities, registry);
    }

    /**
     * Loads scene JSON using entity store and registry from {@link SceneLoadContext}.
     */
    public static SceneLoadMetadata load(String scenePath, SceneLoadContext ctx) {
        ComponentRegistry registry = ctx.registry();
        if (!(registry instanceof ComponentRegistryImpl)) {
            throw new IllegalStateException(
                    "SceneLoader requires a ComponentRegistryImpl, got "
                            + (registry == null ? "null" : registry.getClass().getName()));
        }
        return load(scenePath, ctx.manager().entities(), (ComponentRegistryImpl) registry);
    }
}
