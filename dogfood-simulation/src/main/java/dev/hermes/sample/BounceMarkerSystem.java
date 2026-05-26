package dev.hermes.sample;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;

/**
 * Adds a vertical bounce offset to entities with {@link BounceMarker}.
 */
public final class BounceMarkerSystem implements System {

    @Override
    public void update(World world, float deltaSeconds) {
        for (Entity entity : world.entitiesWith(BounceMarker.class)) {
            BounceMarker bounce = world.getComponent(entity.id(), BounceMarker.class);
            Transform transform = world.getComponent(entity.id(), Transform.class);
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
