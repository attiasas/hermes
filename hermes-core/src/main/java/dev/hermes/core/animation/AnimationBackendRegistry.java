package dev.hermes.core.animation;

import dev.hermes.api.animation.AnimationClipType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Registry for animation backends keyed by clip type. */
public final class AnimationBackendRegistry {

    private final Map<AnimationClipType, AnimationBackend> byType = new EnumMap<>(AnimationClipType.class);

    public void register(AnimationBackend backend) {
        Objects.requireNonNull(backend, "backend");
        byType.put(backend.type(), backend);
    }

    public AnimationBackend require(AnimationClipType type) {
        Objects.requireNonNull(type, "type");
        AnimationBackend backend = byType.get(type);
        if (backend == null) {
            throw new IllegalArgumentException("No animation backend registered for type: " + type);
        }
        return backend;
    }
}
