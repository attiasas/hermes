package dev.hermes.api.scene;

import dev.hermes.api.ecs.World;

/** Handle to a loaded scene instance on the stack. */
public interface SceneHandle {

  String id();

  World world();

  boolean paused();
}
