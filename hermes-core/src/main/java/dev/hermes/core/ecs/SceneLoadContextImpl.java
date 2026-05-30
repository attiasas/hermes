package dev.hermes.core.ecs;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneLoadContext;

/**
 * Core {@link SceneLoadContext} backed by a world manager and registry.
 */
final class SceneLoadContextImpl implements SceneLoadContext {

    private final WorldManager manager;
    private final ComponentRegistry registry;

    SceneLoadContextImpl(WorldManager manager, ComponentRegistry registry) {
        this.manager = manager;
        this.registry = registry;
    }

    @Override
    public WorldManager manager() {
        return manager;
    }

    @Override
    public ComponentRegistry registry() {
        return registry;
    }
}
