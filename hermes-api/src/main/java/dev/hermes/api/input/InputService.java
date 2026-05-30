package dev.hermes.api.input;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.viewport.SceneViewport;

import java.util.Optional;

/**
 * Engine service for per-frame input devices, remapped actions, and screen picking.
 */
public interface InputService {

    /** Polls devices and updates remapped actions for the current frame. Call once per frame before systems. */
    void poll(float deltaSeconds);

    InputActions actions();

    InputDevices devices();

    /**
     * Convenience delegate to {@link HermesEngine#viewport()}{@code .forWorld(entities)}.
     * Prefer {@code engine.viewport()} when both services are available.
     */
    SceneViewport viewport(EntityStore entities);

    /** Screen coords (window). Uses backbuffer surface + active camera via ViewportService. */
    Optional<PickHit> pick(EntityStore entities, float screenX, float screenY);

    Optional<PickHit> pick(EntityStore entities, float screenX, float screenY, PickLayer layer);
}
