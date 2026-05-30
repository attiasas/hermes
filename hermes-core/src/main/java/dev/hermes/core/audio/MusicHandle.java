package dev.hermes.core.audio;

/** Handle to a loaded music track with runtime playback control. */
interface MusicHandle {

    void play(float volume, boolean loop);

    void stop();

    void setVolume(float volume);

    boolean isPlaying();

    void pause();

    void resume();

    void dispose();
}
