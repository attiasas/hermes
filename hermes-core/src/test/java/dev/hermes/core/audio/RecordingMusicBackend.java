package dev.hermes.core.audio;

import java.util.HashMap;
import java.util.Map;

/** Test double that records music load and playback. */
final class RecordingMusicBackend implements MusicBackend {

    volatile String lastLoadedPath;
    volatile String lastCrossfadePlaylist;
    volatile float lastCrossfadeSeconds;
    volatile float lastStopFadeSeconds;
    volatile boolean paused;
    volatile boolean resumed;

    private final Map<String, RecordingMusicHandle> handles = new HashMap<>();

    @Override
    public MusicHandle load(String path) {
        lastLoadedPath = path;
        return handles.computeIfAbsent(path, p -> new RecordingMusicHandle(this, p));
    }

    @Override
    public void disposeMusic() {
        handles.clear();
    }

    void recordCrossfade(String playlistPath, float fadeSeconds) {
        lastCrossfadePlaylist = playlistPath;
        lastCrossfadeSeconds = fadeSeconds;
    }

    void recordStop(float fadeSeconds) {
        lastStopFadeSeconds = fadeSeconds;
    }

    private static final class RecordingMusicHandle implements MusicHandle {

        private final RecordingMusicBackend backend;
        private final String path;
        private boolean playing;
        private float volume;

        RecordingMusicHandle(RecordingMusicBackend backend, String path) {
            this.backend = backend;
            this.path = path;
        }

        @Override
        public void play(float volume, boolean loop) {
            this.volume = volume;
            playing = true;
        }

        @Override
        public void stop() {
            playing = false;
        }

        @Override
        public void setVolume(float volume) {
            this.volume = volume;
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        @Override
        public void pause() {
            playing = false;
            backend.paused = true;
        }

        @Override
        public void resume() {
            playing = true;
            backend.resumed = true;
        }

        @Override
        public void dispose() {}
    }
}
