package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.audio.AudioBus;

/** 3D positional looping ambient audio source. Requires {@link Transform} on the same entity. */
public final class AmbientSource implements Component {

    private String clip = "";
    private boolean clipIsId;
    private AudioBus bus = AudioBus.AMBIENT;
    private float volume = 1f;
    private boolean loop = true;
    private float minDistance = 1f;
    private float maxDistance = 50f;
    private float refDistance = 1f;

    public String clip() {
        return clip;
    }

    public void setClip(String clip) {
        this.clip = clip == null ? "" : clip;
    }

    public boolean clipIsId() {
        return clipIsId;
    }

    public void setClipIsId(boolean clipIsId) {
        this.clipIsId = clipIsId;
    }

    public AudioBus bus() {
        return bus;
    }

    public void setBus(AudioBus bus) {
        this.bus = bus == null ? AudioBus.AMBIENT : bus;
    }

    public float volume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public boolean loop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public float minDistance() {
        return minDistance;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

    public float maxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public float refDistance() {
        return refDistance;
    }

    public void setRefDistance(float refDistance) {
        this.refDistance = refDistance;
    }
}
