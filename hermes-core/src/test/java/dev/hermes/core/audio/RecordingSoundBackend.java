package dev.hermes.core.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** Test double that records playback parameters. */
final class RecordingSoundBackend implements SoundBackend {

    final AtomicLong nextId = new AtomicLong(1L);
    volatile float lastVolume;
    volatile float lastPitch;
    volatile float lastPan;
    volatile boolean lastLoop;
    volatile String lastPath;
    volatile float listenerX;
    volatile float listenerY;
    volatile float listenerZ;
    volatile int stopCount;

    private final Map<String, Object> loaded = new HashMap<>();

    @Override
    public Object loadSound(String path) {
        return loaded.computeIfAbsent(path, key -> new Object());
    }

    @Override
    public long play(String path, float volume, float pitch, float pan, boolean loop) {
        lastPath = path;
        lastVolume = volume;
        lastPitch = pitch;
        lastPan = pan;
        lastLoop = loop;
        loadSound(path);
        return nextId.getAndIncrement();
    }

    @Override
    public void stop(String path, long instanceId) {
        stopCount++;
    }

    @Override
    public void setVolume(String path, long instanceId, float volume) {
        lastVolume = volume;
    }

    @Override
    public void setPitch(String path, long instanceId, float pitch) {
        lastPitch = pitch;
    }

    @Override
    public void setPan(String path, long instanceId, float pan) {
        lastPan = pan;
    }

    @Override
    public void setPosition(String path, long instanceId, float x, float y, float z) {}

    @Override
    public void setListenerPosition(float x, float y, float z) {
        listenerX = x;
        listenerY = y;
        listenerZ = z;
    }

    @Override
    public void disposeSounds() {
        loaded.clear();
    }
}
