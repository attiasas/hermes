package

{{package}};

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.WorldManager;

/**
 * Updates scale on entities with {@link PulseMarker}.
 */
public final class PulseMarkerSystem implements System {

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        for (Entity entity : entities.entitiesWith(PulseMarker.class)) {
            PulseMarker pulse = entities.getComponent(entity.id(), PulseMarker.class);
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (pulse == null || transform == null) {
                continue;
            }
            float phase = pulse.phase() + deltaSeconds * pulse.speed();
            pulse.setPhase(phase);
            float scale = 1f + pulse.amplitude() * (float) Math.sin(phase);
            transform.setScaleX(scale);
            transform.setScaleY(scale);
        }
    }
}
