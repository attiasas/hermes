package dev.hermes.core.ecs;

import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneHandle;

/** Mutable {@link SceneHandle} for the scene stack. */
final class SceneHandleImpl implements SceneHandle {

  private final String id;
  private final World world;
  private boolean paused;

  SceneHandleImpl(String id, World world) {
    this.id = id;
    this.world = world;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public World world() {
    return world;
  }

  @Override
  public boolean paused() {
    return paused;
  }

  void setPaused(boolean paused) {
    this.paused = paused;
  }
}
