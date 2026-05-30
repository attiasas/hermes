package dev.hermes.core.audio;

/** libGDX-free music playback surface for production and test backends. */
interface MusicBackend {

    MusicHandle load(String path);

    void disposeMusic();
}
