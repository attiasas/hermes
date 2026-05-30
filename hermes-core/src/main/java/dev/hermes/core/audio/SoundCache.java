package dev.hermes.core.audio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Load-once cache for decoded sound assets. */
final class SoundCache {

    private final SoundBackend backend;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    SoundCache(SoundBackend backend) {
        this.backend = backend;
    }

    Object soundForPath(String path) {
        return cache.computeIfAbsent(path, backend::loadSound);
    }

    void dispose() {
        cache.clear();
        backend.disposeSounds();
    }
}
