package dev.hermes.api.animation;

import java.util.List;
import java.util.Objects;

/** Immutable animation clip root document model. */
public final class AnimationClip {

    private final int version;
    private final float duration;
    private final boolean loop;
    private final List<AnimationTrack> tracks;

    public AnimationClip(int version, float duration, boolean loop, List<AnimationTrack> tracks) {
        this.version = version;
        this.duration = duration;
        this.loop = loop;
        this.tracks = List.copyOf(Objects.requireNonNull(tracks, "tracks"));
        if (this.tracks.isEmpty()) {
            throw new IllegalArgumentException("tracks must not be empty");
        }
    }

    public int version() {
        return version;
    }

    public float duration() {
        return duration;
    }

    public boolean loop() {
        return loop;
    }

    public List<AnimationTrack> tracks() {
        return tracks;
    }
}
