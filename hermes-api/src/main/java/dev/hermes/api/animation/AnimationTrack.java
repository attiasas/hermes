package dev.hermes.api.animation;

import java.util.List;
import java.util.Objects;

/** Immutable target property track with interpolation and keyframes. */
public final class AnimationTrack {

    private final String target;
    private final Interpolation interpolation;
    private final List<Keyframe> keyframes;

    public AnimationTrack(String target, Interpolation interpolation, List<Keyframe> keyframes) {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("target must be non-empty");
        }
        this.target = target.trim();
        this.interpolation = Objects.requireNonNull(interpolation, "interpolation");
        this.keyframes = List.copyOf(Objects.requireNonNull(keyframes, "keyframes"));
        if (this.keyframes.isEmpty()) {
            throw new IllegalArgumentException("keyframes must not be empty");
        }
    }

    public String target() {
        return target;
    }

    public Interpolation interpolation() {
        return interpolation;
    }

    public List<Keyframe> keyframes() {
        return keyframes;
    }
}
