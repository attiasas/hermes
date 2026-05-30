package dev.hermes.core.audio;

import dev.hermes.api.audio.SoundHandle;

/** SoundHandle backed by a {@link SoundBackend} instance id. */
final class GdxSoundHandle implements SoundHandle {

    private final SoundBackend backend;
    private final String path;
    private final long instanceId;
    private final Runnable onStop;
    private volatile boolean playing = true;
    private volatile float volume = 1f;

    GdxSoundHandle(SoundBackend backend, String path, long instanceId, float volume) {
        this(backend, path, instanceId, volume, null);
    }

    GdxSoundHandle(SoundBackend backend, String path, long instanceId, float volume, Runnable onStop) {
        this.backend = backend;
        this.path = path;
        this.instanceId = instanceId;
        this.volume = volume;
        this.onStop = onStop;
    }

    @Override
    public long instanceId() {
        return instanceId;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void stop() {
        if (!playing) {
            return;
        }
        backend.stop(path, instanceId);
        playing = false;
        if (onStop != null) {
            onStop.run();
        }
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        backend.setVolume(path, instanceId, volume);
    }

    @Override
    public void setPitch(float pitch) {
        backend.setPitch(path, instanceId, pitch);
    }

    @Override
    public void setPan(float pan) {
        backend.setPan(path, instanceId, pan);
    }

    @Override
    public void setWorldPosition(float x, float y, float z) {
        backend.setPosition(path, instanceId, x, y, z);
    }

    float volume() {
        return volume;
    }
}
