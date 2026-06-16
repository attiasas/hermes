package dev.hermes.core.audio;

/** libGDX-free sound playback surface for production and test backends. */
public interface SoundBackend {

    Object loadSound(String path);

    long play(String path, float volume, float pitch, float pan, boolean loop);

    void stop(String path, long instanceId);

    void setVolume(String path, long instanceId, float volume);

    void setPitch(String path, long instanceId, float pitch);

    void setPan(String path, long instanceId, float pan);

    void setPosition(String path, long instanceId, float x, float y, float z);

    void setListenerPosition(float x, float y, float z);

    void disposeSounds();
}
