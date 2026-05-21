package dev.hermes.api.scene;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.World;

/** Context passed to {@link SceneSource#populate(SceneLoadContext)} when a scene is loaded. */
public interface SceneLoadContext {

  World world();

  ComponentRegistry registry();
}
