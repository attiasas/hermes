package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.audio.AudioBus;
import dev.hermes.api.audio.PlayOptions;
import org.junit.jupiter.api.Test;

final class AudioServiceImplTest {

    @Test
    void playAppliesBusGainToVolume() {
        RecordingSoundBackend backend = new RecordingSoundBackend();
        AudioMixerImpl mixer = new AudioMixerImpl();
        mixer.setVolume(AudioBus.SFX, 0.5f);
        AudioServiceImpl audio = new AudioServiceImpl(backend, mixer, new SoundCache(backend));
        audio.play("sfx/a.wav", PlayOptions.builder().bus(AudioBus.SFX).volume(0.8f).build());
        assertEquals(0.4f, backend.lastVolume, 0.001f);
    }

    @Test
    void stopAllStopsTrackedInstances() {
        RecordingSoundBackend backend = new RecordingSoundBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(backend, new AudioMixerImpl(), new SoundCache(backend));
        audio.play("sfx/a.wav", PlayOptions.builder().build());
        audio.play("sfx/a.wav", PlayOptions.builder().build());
        audio.stopAll("sfx/a.wav");
        assertEquals(2, backend.stopCount);
    }

    @Test
    void enforcesMaxInstancesPerClipFromProfile() {
        RecordingSoundBackend backend = new RecordingSoundBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(backend, new AudioMixerImpl(), new SoundCache(backend));
        audio.loadProfileFromJson(
                "{\"version\":1,\"limits\":{\"maxInstancesPerClip\":2},\"clips\":{},\"buses\":{}}");
        audio.play("sfx/a.wav", PlayOptions.builder().build());
        audio.play("sfx/a.wav", PlayOptions.builder().build());
        audio.play("sfx/a.wav", PlayOptions.builder().build());
        assertEquals(1, backend.stopCount);
    }
}
