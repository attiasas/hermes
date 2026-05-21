package dev.hermes.api.scene;

import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.World;

/** Runtime context for a scene during lifecycle callbacks. */
public interface SceneContext {

  String sceneId();

  World world();

  ComponentRegistry registry();

  HermesEngine engine();

  HermesSession session();
}
