package dev.hermes.api.animation;

import java.util.Objects;

/** Immutable reference to a Hermes or glTF clip with playback overrides. */
public final class AnimationClipRef {

    private final AnimationClipType type;
    private final String path;
    private final String clipName;
    private final boolean loop;
    private final float speed;

    private AnimationClipRef(
            AnimationClipType type,
            String path,
            String clipName,
            boolean loop,
            float speed) {
        this.type = Objects.requireNonNull(type, "type");
        this.path = path;
        this.clipName = clipName;
        this.loop = loop;
        this.speed = speed;
    }

    public static AnimationClipRef hermes(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("AnimationClipRef.hermes path is required");
        }
        return new AnimationClipRef(AnimationClipType.HERMES, path.trim(), null, true, 1f);
    }

    public static AnimationClipRef gltf(String clipName) {
        if (clipName == null || clipName.isBlank()) {
            throw new IllegalArgumentException("AnimationClipRef.gltf clipName is required");
        }
        return new AnimationClipRef(AnimationClipType.GLTF, null, clipName.trim(), true, 1f);
    }

    public AnimationClipRef withLoop(boolean loop) {
        return new AnimationClipRef(type, path, clipName, loop, speed);
    }

    public AnimationClipRef withSpeed(float speed) {
        return new AnimationClipRef(type, path, clipName, loop, speed);
    }

    public AnimationClipType type() {
        return type;
    }

    public String path() {
        return path;
    }

    public String clipName() {
        return clipName;
    }

    public boolean loop() {
        return loop;
    }

    public float speed() {
        return speed;
    }
}
