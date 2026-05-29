package dev.hermes.core.ecs;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneRegistry;
import dev.hermes.core.scene.AssetSceneSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory scene definition registry.
 */
public final class SceneRegistryImpl implements SceneRegistry {

    private static final Logger log = Logs.get(SceneRegistryImpl.class);

    private final ComponentRegistryImpl registry;
    private final Map<String, SceneDefinition> definitions = new LinkedHashMap<>();

    public SceneRegistryImpl(ComponentRegistryImpl registry) {
        this.registry = registry;
    }

    public ComponentRegistry componentRegistry() {
        return registry;
    }

    @Override
    public void register(SceneDefinition definition) {
        if (definitions.containsKey(definition.id())) {
            throw new IllegalArgumentException("Scene '" + definition.id() + "' is already registered");
        }
        log.debug("Registering scene: " + definition.id() + " with source: " + definition.source().getClass().getName());
        definitions.put(definition.id(), definition);
    }

    @Override
    public void register(String id, String assetPath) {
        log.debug("Registering scene: " + id + " with asset path: " + assetPath);
        register(new SceneDefinition(id, new AssetSceneSource(assetPath)));
    }

    public SceneDefinition get(String id) {
        return definitions.get(id);
    }
}
