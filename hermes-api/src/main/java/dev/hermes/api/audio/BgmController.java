package dev.hermes.api.audio;

/** Background music playback and crossfade control. */
public interface BgmController {

    void playPlaylist(String playlistAssetPath);

    void playRandom(String playlistAssetPath);

    void crossfadeTo(String playlistAssetPath, float fadeSeconds);

    void stop(float fadeSeconds);

    void pause();

    void resume();

    void setVolume(float volume01);

    boolean isPlaying();
}
