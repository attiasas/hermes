package dev.hermes.core.ecs;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.EntityTypeRegistry;
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
     * Parses top-level scene metadata from JSON without loading entities.
     */
    public static SceneLoadMetadata loadMetadataFromString(String json) {
        SceneDocument document = SceneDocument.parse("", json);
        return SceneLoadMetadata.fromDocument(document);
    }

    /**
     * Loads scene JSON from a string (used by tests and {@link #load}).
     */
    public static SceneLoadMetadata loadFromString(
            String scenePath, String json, EntityStore entities, ComponentRegistryImpl registry) {
        return loadFromString(scenePath, json, entities, registry, new EntityTypeRegistryImpl());
    }

    public static SceneLoadMetadata loadFromString(
            String scenePath,
            String json,
            EntityStore entities,
            ComponentRegistryImpl registry,
            EntityTypeRegistry entityTypes) {
        EntityFactory factory = new EntityFactory(entityTypes, registry);
        return SceneParser.loadIntoEntities(scenePath, json, entities, factory);
    }

    public static SceneLoadMetadata load(String scenePath, EntityStore entities, ComponentRegistryImpl registry) {
        return load(scenePath, entities, registry, new EntityTypeRegistryImpl());
    }

    public static SceneLoadMetadata load(
            String scenePath,
            EntityStore entities,
            ComponentRegistryImpl registry,
            EntityTypeRegistry entityTypes) {
        if (scenePath == null || scenePath.isBlank()) {
            return SceneLoadMetadata.empty();
        }
        FileHandle handle = HermesAssetPaths.internal(scenePath);
        if (!handle.exists()) {
            throw new SceneLoadException("Scene '" + scenePath + "': file not found");
        }
        String json = handle.readString(StandardCharsets.UTF_8.name());
        log.debug("Loading scene: " + scenePath + " with JSON: " + json);
        EntityFactory factory = new EntityFactory(entityTypes, registry);
        return SceneParser.loadIntoEntities(scenePath, json, entities, factory);
    }

    /**
     * Loads scene JSON using entity store and registry from {@link SceneLoadContext}.
     */
    public static SceneLoadMetadata load(String scenePath, SceneLoadContext ctx) {
        return load(scenePath, ctx, new EntityTypeRegistryImpl());
    }

    public static SceneLoadMetadata load(String scenePath, SceneLoadContext ctx, EntityTypeRegistry entityTypes) {
        ComponentRegistry registry = ctx.registry();
        if (!(registry instanceof ComponentRegistryImpl)) {
            throw new IllegalStateException(
                    "SceneLoader requires a ComponentRegistryImpl, got "
                            + (registry == null ? "null" : registry.getClass().getName()));
        }
        return load(scenePath, ctx.manager().entities(), (ComponentRegistryImpl) registry, entityTypes);
    }
}
