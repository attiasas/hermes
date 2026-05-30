package dev.hermes.api.audio;

import java.util.Optional;

/** Immutable playback parameters for one-shot and looped SFX. */
public final class PlayOptions {

    private final AudioBus bus;
    private final float volume;
    private final float pitch;
    private final float pan;
    private final boolean loop;
    private final Float worldX;
    private final Float worldY;
    private final Float worldZ;

    PlayOptions(Builder builder) {
        this.bus = builder.bus;
        this.volume = builder.volume;
        this.pitch = builder.pitch;
        this.pan = builder.pan;
        this.loop = builder.loop;
        this.worldX = builder.worldX;
        this.worldY = builder.worldY;
        this.worldZ = builder.worldZ;
    }

    public static Builder builder() {
        return new Builder();
    }

    public AudioBus bus() {
        return bus;
    }

    public float volume() {
        return volume;
    }

    public float pitch() {
        return pitch;
    }

    public float pan() {
        return pan;
    }

    public boolean loop() {
        return loop;
    }

    public Optional<Float> worldX() {
        return Optional.ofNullable(worldX);
    }

    public Optional<Float> worldY() {
        return Optional.ofNullable(worldY);
    }

    public Optional<Float> worldZ() {
        return Optional.ofNullable(worldZ);
    }

    public static final class Builder {

        private AudioBus bus = AudioBus.SFX;
        private float volume = 1f;
        private float pitch = 1f;
        private float pan = 0f;
        private boolean loop = false;
        private Float worldX;
        private Float worldY;
        private Float worldZ;

        public Builder bus(AudioBus bus) {
            this.bus = bus;
            return this;
        }

        public Builder volume(float volume) {
            this.volume = volume;
            return this;
        }

        public Builder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder pan(float pan) {
            this.pan = pan;
            return this;
        }

        public Builder loop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public Builder worldPosition(float x, float y, float z) {
            this.worldX = x;
            this.worldY = y;
            this.worldZ = z;
            return this;
        }

        public PlayOptions build() {
            return new PlayOptions(this);
        }
    }
}
