package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.audio.AudioBus;

import java.util.Arrays;

/** Config-only footstep sounds driven by entity movement. Requires {@link Transform}. */
public final class FootstepEmitter implements Component {

    private String[] clips = new String[0];
    private boolean clipIsId;
    private float intervalSeconds = 0.35f;
    private float minSpeed = 0.5f;
    private AudioBus bus = AudioBus.SFX;
    private float volume = 0.6f;

    public String[] clips() {
        return clips;
    }

    public void setClips(String[] clips) {
        this.clips = clips == null ? new String[0] : Arrays.copyOf(clips, clips.length);
    }

    public boolean clipIsId() {
        return clipIsId;
    }

    public void setClipIsId(boolean clipIsId) {
        this.clipIsId = clipIsId;
    }

    public float intervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(float intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public float minSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(float minSpeed) {
        this.minSpeed = minSpeed;
    }

    public AudioBus bus() {
        return bus;
    }

    public void setBus(AudioBus bus) {
        this.bus = bus == null ? AudioBus.SFX : bus;
    }

    public float volume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
