package dev.hermes.core.audio;

import dev.hermes.api.audio.AudioBus;
import dev.hermes.api.audio.AudioMixer;
import dev.hermes.api.audio.BgmController;

import java.util.List;
import java.util.Random;

/** Background music controller with playlist support and crossfade. */
final class BgmControllerImpl implements BgmController {

    private final MusicBackend backend;
    private final AudioMixer mixer;
    private final Random random = new Random();

    private BgmPlaylist activePlaylist;
    private int sequentialIndex;
    private MusicHandle current;
    private float userVolume = 1f;
    private boolean paused;

    private float fadeRemaining;
    private float fadeDuration;
    private MusicHandle fadeOutHandle;
    private MusicHandle fadeInHandle;

    BgmControllerImpl(MusicBackend backend, AudioMixer mixer) {
        this.backend = backend;
        this.mixer = mixer;
    }

    void tick(float deltaSeconds) {
        if (paused) {
            return;
        }
        if (fadeRemaining > 0f) {
            updateCrossfade(deltaSeconds);
            return;
        }
        if (current != null && !current.isPlaying() && activePlaylist != null) {
            advanceAfterTrackEnded();
        }
    }

    @Override
    public void playPlaylist(String playlistAssetPath) {
        BgmPlaylist playlist = BgmPlaylist.load(playlistAssetPath);
        activePlaylist = playlist;
        sequentialIndex = 0;
        String track = selectTrack(playlist, false);
        playImmediate(track);
    }

    @Override
    public void playRandom(String playlistAssetPath) {
        BgmPlaylist playlist = BgmPlaylist.load(playlistAssetPath);
        activePlaylist = playlist;
        String track = selectTrack(playlist, true);
        playImmediate(track);
    }

    @Override
    public void crossfadeTo(String playlistAssetPath, float fadeSeconds) {
        BgmPlaylist playlist = BgmPlaylist.load(playlistAssetPath);
        activePlaylist = playlist;
        sequentialIndex = 0;
        String track = selectTrack(playlist, playlist.mode() == BgmPlaylist.Mode.RANDOM);
        startCrossfade(track, fadeSeconds);
    }

    @Override
    public void stop(float fadeSeconds) {
        activePlaylist = null;
        if (current == null) {
            return;
        }
        if (fadeSeconds <= 0f) {
            stopCurrent();
            return;
        }
        fadeOutHandle = current;
        fadeInHandle = null;
        fadeDuration = fadeSeconds;
        fadeRemaining = fadeSeconds;
        current = null;
    }

    @Override
    public void pause() {
        paused = true;
        if (current != null) {
            current.pause();
        }
    }

    @Override
    public void resume() {
        paused = false;
        if (current != null) {
            current.resume();
            current.setVolume(effectiveVolume());
        }
    }

    @Override
    public void setVolume(float volume01) {
        userVolume = Math.max(0f, Math.min(1f, volume01));
        if (current != null && fadeRemaining <= 0f) {
            current.setVolume(effectiveVolume());
        }
    }

    @Override
    public boolean isPlaying() {
        if (fadeRemaining > 0f) {
            return fadeInHandle != null || fadeOutHandle != null;
        }
        return current != null && current.isPlaying();
    }

    void dispose() {
        stopImmediate();
        backend.disposeMusic();
    }

    private void playImmediate(String trackPath) {
        stopImmediate();
        current = backend.load(trackPath);
        current.play(effectiveVolume(), true);
    }

    private void startCrossfade(String trackPath, float fadeSeconds) {
        MusicHandle next = backend.load(trackPath);
        if (current == null || fadeSeconds <= 0f) {
            stopImmediate();
            current = next;
            current.play(effectiveVolume(), true);
            return;
        }
        fadeOutHandle = current;
        fadeInHandle = next;
        fadeInHandle.play(0f, true);
        fadeDuration = fadeSeconds;
        fadeRemaining = fadeSeconds;
        current = next;
    }

    private void updateCrossfade(float deltaSeconds) {
        fadeRemaining -= deltaSeconds;
        float progress = fadeDuration > 0f ? 1f - (fadeRemaining / fadeDuration) : 1f;
        progress = Math.max(0f, Math.min(1f, progress));
        float volume = effectiveVolume();
        if (fadeOutHandle != null) {
            fadeOutHandle.setVolume(volume * (1f - progress));
        }
        if (fadeInHandle != null) {
            fadeInHandle.setVolume(volume * progress);
        }
        if (fadeRemaining <= 0f) {
            finishCrossfade();
        }
    }

    private void finishCrossfade() {
        if (fadeOutHandle != null) {
            fadeOutHandle.stop();
            fadeOutHandle = null;
        }
        if (fadeInHandle != null) {
            fadeInHandle.setVolume(effectiveVolume());
        }
        fadeRemaining = 0f;
        fadeDuration = 0f;
        fadeInHandle = null;
    }

    private void advanceAfterTrackEnded() {
        if (activePlaylist == null || activePlaylist.tracks().isEmpty()) {
            return;
        }
        if (activePlaylist.mode() == BgmPlaylist.Mode.SINGLE) {
            return;
        }
        String nextTrack = selectTrack(activePlaylist, activePlaylist.mode() == BgmPlaylist.Mode.RANDOM);
        startCrossfade(nextTrack, activePlaylist.crossfadeSeconds());
    }

    private String selectTrack(BgmPlaylist playlist, boolean randomPick) {
        List<String> tracks = playlist.tracks();
        if (tracks.isEmpty()) {
            throw new AudioProfileParseException("BGM playlist has no tracks");
        }
        if (randomPick || playlist.mode() == BgmPlaylist.Mode.RANDOM) {
            return tracks.get(random.nextInt(tracks.size()));
        }
        if (playlist.mode() == BgmPlaylist.Mode.SINGLE) {
            return tracks.get(0);
        }
        String track = tracks.get(sequentialIndex % tracks.size());
        sequentialIndex = (sequentialIndex + 1) % tracks.size();
        return track;
    }

    private float effectiveVolume() {
        return mixer.effectiveGain(AudioBus.MUSIC) * userVolume;
    }

    private void stopCurrent() {
        if (current != null) {
            current.stop();
            current = null;
        }
    }

    private void stopImmediate() {
        finishCrossfade();
        stopCurrent();
        fadeRemaining = 0f;
    }
}
