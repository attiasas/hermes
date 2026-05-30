package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.audio.AudioBus;

/** One-shot or looped SFX emitter with optional automatic playback triggers. */
public final class SoundEmitter implements Component {

    public enum PlayOn {
        MANUAL,
        SPAWN,
        INTERVAL
    }

    private String clip = "";
    private boolean clipIsId;
    private AudioBus bus = AudioBus.SFX;
    private float volume = 1f;
    private float pitch = 1f;
    private boolean loop;
    private PlayOn playOn = PlayOn.MANUAL;
    private float intervalSeconds;

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
        this.bus = bus == null ? AudioBus.SFX : bus;
    }

    public float volume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float pitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean loop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public PlayOn playOn() {
        return playOn;
    }

    public void setPlayOn(PlayOn playOn) {
        this.playOn = playOn == null ? PlayOn.MANUAL : playOn;
    }

    public float intervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(float intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }
}
