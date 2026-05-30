package dev.hermes.core.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

/** libGDX {@link Sound} backend for desktop and Android targets. */
final class GdxSoundBackend implements SoundBackend {

    private final Map<String, Sound> sounds = new HashMap<>();

    @Override
    public Object loadSound(String path) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
        sounds.put(path, sound);
        return sound;
    }

    @Override
    public long play(String path, float volume, float pitch, float pan, boolean loop) {
        Sound sound = sounds.get(path);
        if (sound == null) {
            loadSound(path);
            sound = sounds.get(path);
        }
        if (loop) {
            return sound.loop(volume, pitch, pan);
        }
        return sound.play(volume, pitch, pan);
    }

    @Override
    public void stop(String path, long instanceId) {
        Sound sound = sounds.get(path);
        if (sound != null) {
            sound.stop(instanceId);
        }
    }

    @Override
    public void setVolume(String path, long instanceId, float volume) {
        Sound sound = sounds.get(path);
        if (sound != null) {
            sound.setVolume(instanceId, volume);
        }
    }

    @Override
    public void setPitch(String path, long instanceId, float pitch) {
        Sound sound = sounds.get(path);
        if (sound != null) {
            sound.setPitch(instanceId, pitch);
        }
    }

    @Override
    public void setPan(String path, long instanceId, float pan) {
        Sound sound = sounds.get(path);
        if (sound != null) {
            sound.setPan(instanceId, pan, 1f);
        }
    }

    @Override
    public void setPosition(String path, long instanceId, float x, float y, float z) {
        // libGDX Sound has no 3D positioning in 1.14; reserved for future backends.
    }

    @Override
    public void setListenerPosition(float x, float y, float z) {
        // libGDX Audio has no listener API in 1.14; reserved for future backends.
    }

    @Override
    public void disposeSounds() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
    }
}
