package dev.hermes.api.ecs;

/**
 * Per-scene manager exposing the entity store and future world services.
 */
public interface WorldManager {

    EntityStore entities();
}
