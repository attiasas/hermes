package dev.hermes.sample;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.WorldManager;

/**
 * Moves entities with {@link SpinMarker} in a circle around their configured center.
 */
public final class SpinMarkerSystem implements System {

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        for (Entity entity : entities.entitiesWith(SpinMarker.class)) {
            SpinMarker spin = entities.getComponent(entity.id(), SpinMarker.class);
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (spin == null || transform == null) {
                continue;
            }

            spin.setAngleRadians(spin.angleRadians() + spin.speedRadiansPerSecond() * deltaSeconds);
            float x = spin.centerX() + (float) Math.cos(spin.angleRadians()) * spin.radius();
            float y = spin.centerY() + (float) Math.sin(spin.angleRadians()) * spin.radius();
            transform.setX(x);
            transform.setY(y);
        }
    }
}
