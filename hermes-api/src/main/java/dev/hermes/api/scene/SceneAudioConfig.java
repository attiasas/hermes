package dev.hermes.api.scene;

import java.util.Optional;

/** Per-scene audio configuration from scene JSON {@code "audio"} field. */
public final class SceneAudioConfig {

    private final String bgmPlaylistId;
    private final String bgmPlaylistPath;
    private final float fadeInSeconds;
    private final float fadeOutSeconds;
    private final boolean pauseBgmOnPause;

    public SceneAudioConfig(
            String bgmPlaylistId,
            String bgmPlaylistPath,
            float fadeInSeconds,
            float fadeOutSeconds,
            boolean pauseBgmOnPause) {
        this.bgmPlaylistId = blankToNull(bgmPlaylistId);
        this.bgmPlaylistPath = blankToNull(bgmPlaylistPath);
        this.fadeInSeconds = fadeInSeconds;
        this.fadeOutSeconds = fadeOutSeconds;
        this.pauseBgmOnPause = pauseBgmOnPause;
    }

    public Optional<String> bgmPlaylistId() {
        return Optional.ofNullable(bgmPlaylistId);
    }

    public Optional<String> bgmPlaylistPath() {
        return Optional.ofNullable(bgmPlaylistPath);
    }

    public float fadeInSeconds() {
        return fadeInSeconds;
    }

    public float fadeOutSeconds() {
        return fadeOutSeconds;
    }

    public boolean pauseBgmOnPause() {
        return pauseBgmOnPause;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
