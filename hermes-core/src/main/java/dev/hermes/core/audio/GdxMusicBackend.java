package dev.hermes.core.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import java.util.HashMap;
import java.util.Map;

/** libGDX {@link Music} backend for streaming BGM. */
final class GdxMusicBackend implements MusicBackend {

    private final Map<String, GdxMusicHandle> music = new HashMap<>();

    @Override
    public MusicHandle load(String path) {
        return music.computeIfAbsent(
                path,
                key -> {
                    Music track = Gdx.audio.newMusic(Gdx.files.internal(path));
                    return new GdxMusicHandle(track);
                });
    }

    @Override
    public void disposeMusic() {
        for (GdxMusicHandle handle : music.values()) {
            handle.dispose();
        }
        music.clear();
    }
}
