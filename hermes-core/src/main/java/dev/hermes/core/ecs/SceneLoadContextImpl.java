package dev.hermes.core.ecs;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneLoadContext;

/** Core {@link SceneLoadContext} backed by a world and registry. */
final class SceneLoadContextImpl implements SceneLoadContext {

  private final World world;
  private final ComponentRegistry registry;

  SceneLoadContextImpl(World world, ComponentRegistry registry) {
    this.world = world;
    this.registry = registry;
  }

  @Override
  public World world() {
    return world;
  }

  @Override
  public ComponentRegistry registry() {
    return registry;
  }
}
