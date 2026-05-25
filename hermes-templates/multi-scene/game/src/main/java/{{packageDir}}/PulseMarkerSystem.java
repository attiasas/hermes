package

{{package}};

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;

/**
 * Updates scale on entities with {@link PulseMarker}.
 */
public final class PulseMarkerSystem implements System {

    @Override
    public void update(World world, float deltaSeconds) {
        for (Entity entity : world.entitiesWith(PulseMarker.class)) {
            PulseMarker pulse = world.getComponent(entity.id(), PulseMarker.class);
            Transform transform = world.getComponent(entity.id(), Transform.class);
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
