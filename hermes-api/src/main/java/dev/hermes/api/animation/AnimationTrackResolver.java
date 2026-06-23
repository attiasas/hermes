package dev.hermes.api.animation;

import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.EntityStore;

/** SPI hook for handling custom animation target strings. */
public interface AnimationTrackResolver {

    /**
     * Applies a sampled value to a custom target path.
     *
     * @return true when handled, false to fall through to built-in targets.
     */
    boolean apply(String target, float value, float[] valueArray, EntityId entityId, EntityStore entities);
}
