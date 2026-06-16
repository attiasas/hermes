package dev.hermes.core.audio;

/** Factory for default audio backends used by resource loading. */
public final class AudioBackends {

    private AudioBackends() {}

    public static SoundBackend gdx() {
        return new GdxSoundBackend();
    }
}
