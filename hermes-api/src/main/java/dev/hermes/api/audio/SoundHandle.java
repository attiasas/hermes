package dev.hermes.api.audio;

/** Handle to a playing sound instance for runtime control. */
public interface SoundHandle {

    long instanceId();

    boolean isPlaying();

    void stop();

    void setVolume(float volume);

    void setPitch(float pitch);

    void setPan(float pan);

    void setWorldPosition(float x, float y, float z);
}
