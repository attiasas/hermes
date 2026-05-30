package dev.hermes.core.audio;

import com.badlogic.gdx.audio.Music;

/** MusicHandle backed by libGDX {@link Music}. */
final class GdxMusicHandle implements MusicHandle {

    private final Music music;
    private volatile float volume = 1f;

    GdxMusicHandle(Music music) {
        this.music = music;
    }

    @Override
    public void play(float volume, boolean loop) {
        this.volume = volume;
        music.setLooping(loop);
        music.setVolume(volume);
        music.play();
    }

    @Override
    public void stop() {
        music.stop();
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        music.setVolume(volume);
    }

    @Override
    public boolean isPlaying() {
        return music.isPlaying();
    }

    @Override
    public void pause() {
        music.pause();
    }

    @Override
    public void resume() {
        music.play();
        music.setVolume(volume);
    }

    @Override
    public void dispose() {
        music.dispose();
    }
}
