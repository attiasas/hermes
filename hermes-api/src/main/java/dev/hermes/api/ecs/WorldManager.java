package dev.hermes.api.ecs;

import dev.hermes.api.world.WorldSpace;
import dev.hermes.api.world.SceneCameraController;

/**
 * Per-scene manager exposing the entity store and world services.
 */
public interface WorldManager {
    EntityStore entities();
    WorldSpace space();
    SceneCameraController camera();
}
