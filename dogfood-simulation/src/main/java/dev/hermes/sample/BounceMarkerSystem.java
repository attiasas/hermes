package dev.hermes.sample;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.WorldManager;

/**
 * Adds a vertical bounce offset to entities with {@link BounceMarker}.
 */
public final class BounceMarkerSystem implements System {

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        for (Entity entity : entities.entitiesWith(BounceMarker.class)) {
            BounceMarker bounce = entities.getComponent(entity.id(), BounceMarker.class);
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (bounce == null || transform == null) {
                continue;
            }

            if (!bounce.baseYCaptured()) {
                bounce.setBaseY(transform.y());
                bounce.setBaseYCaptured(true);
            }

            bounce.setElapsedSeconds(bounce.elapsedSeconds() + deltaSeconds);
            float offset = (float) Math.sin(bounce.elapsedSeconds() * bounce.speed()) * bounce.amplitude();
            transform.setY(bounce.baseY() + offset);
        }
    }
}
