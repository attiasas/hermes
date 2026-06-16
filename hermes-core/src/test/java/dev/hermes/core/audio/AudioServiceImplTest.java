package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.audio.AudioBus;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.ResourceManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class AudioServiceImplTest {

    private static final String CLIP = "sfx/test.wav";

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void playAppliesBusGainToVolume() {
        RecordingSoundBackend backend = new RecordingSoundBackend();
        AudioMixerImpl mixer = new AudioMixerImpl();
        mixer.setVolume(AudioBus.SFX, 0.5f);
        AudioServiceImpl audio =
                new AudioServiceImpl(backend, mixer, ResourceManagerImpl.createDefault(backend));
        audio.play(CLIP, PlayOptions.builder().bus(AudioBus.SFX).volume(0.8f).build());
        assertEquals(0.4f, backend.lastVolume, 0.001f);
    }

    @Test
    void stopAllStopsTrackedInstances() {
        RecordingSoundBackend backend = new RecordingSoundBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(backend, new AudioMixerImpl(), ResourceManagerImpl.createDefault(backend));
        audio.play(CLIP, PlayOptions.builder().build());
        audio.play(CLIP, PlayOptions.builder().build());
        audio.stopAll(CLIP);
        assertEquals(2, backend.stopCount);
    }

    @Test
    void enforcesMaxInstancesPerClipFromProfile() {
        RecordingSoundBackend backend = new RecordingSoundBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(backend, new AudioMixerImpl(), ResourceManagerImpl.createDefault(backend));
        audio.loadProfileFromJson(
                "{\"version\":1,\"limits\":{\"maxInstancesPerClip\":2},\"clips\":{},\"buses\":{}}");
        audio.play(CLIP, PlayOptions.builder().build());
        audio.play(CLIP, PlayOptions.builder().build());
        audio.play(CLIP, PlayOptions.builder().build());
        assertEquals(1, backend.stopCount);
    }
}
