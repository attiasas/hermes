package dev.hermes.api.scene;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.WorldManager;

/**
 * Context passed to {@link SceneSource#populate(SceneLoadContext)} when a scene is loaded.
 */
public interface SceneLoadContext {

    WorldManager manager();

    ComponentRegistry registry();
}
