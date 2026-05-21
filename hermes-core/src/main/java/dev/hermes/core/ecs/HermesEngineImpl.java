package dev.hermes.core.ecs;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.World;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/** Default engine implementation wiring world, registry, and systems. */
public final class HermesEngineImpl implements HermesEngine {

  private final WorldImpl world;
  private final ComponentRegistryImpl registry;
  private final List<System> systems = new ArrayList<>();

  public HermesEngineImpl() {
    this.world = new WorldImpl();
    this.registry = new ComponentRegistryImpl();
    BuiltinComponents.register(registry);
    loadServiceRegistrations();
  }

  private void loadServiceRegistrations() {
    for (dev.hermes.api.ecs.ComponentRegistration registration :
        ServiceLoader.load(dev.hermes.api.ecs.ComponentRegistration.class)) {
      registration.register(this);
    }
  }

  @Override
  public World world() {
    return world;
  }

  @Override
  public ComponentRegistry registry() {
    return registry;
  }

  @Override
  public void loadScene(String scenePath) {
    if (scenePath == null || scenePath.isBlank()) {
      return;
    }
    SceneLoader.load(scenePath, world, registry);
  }

  @Override
  public void addSystem(System system) {
    systems.add(system);
  }

  public List<System> systems() {
    return Collections.unmodifiableList(systems);
  }

  /** Debug-only access to the concrete registry (serialization and field edits). */
  public ComponentRegistryImpl registryImpl() {
    return registry;
  }
}
