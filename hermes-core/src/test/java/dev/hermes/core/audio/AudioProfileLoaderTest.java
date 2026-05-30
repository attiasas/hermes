package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.audio.AudioBus;
import org.junit.jupiter.api.Test;

final class AudioProfileLoaderTest {

    @Test
    void loadsClipsAndActionSounds() {
        String json =
                "{\"version\":1,\"clips\":{\"hit\":\"sfx/hit.wav\"},\"buses\":{\"sfx\":0.5},"
                        + "\"actionSounds\":{\"ui.click\":\"hit\"},\"limits\":{\"maxInstancesPerClip\":4}}";
        AudioProfile profile = AudioProfileLoader.parse(json);
        assertEquals("sfx/hit.wav", profile.resolveClip("hit").orElseThrow());
        assertEquals(0.5f, profile.busVolume(AudioBus.SFX), 0.001f);
        assertEquals("hit", profile.actionSoundClipId("ui.click").orElseThrow());
        assertEquals(4, profile.maxInstancesPerClip());
    }
}
