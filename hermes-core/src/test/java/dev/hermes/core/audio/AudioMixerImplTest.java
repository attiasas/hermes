package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.audio.AudioBus;
import org.junit.jupiter.api.Test;

final class AudioMixerImplTest {

    @Test
    void effectiveGainMultipliesMasterAndBus() {
        AudioMixerImpl mixer = new AudioMixerImpl();
        mixer.setVolume(AudioBus.MASTER, 0.5f);
        mixer.setVolume(AudioBus.SFX, 0.8f);
        assertEquals(0.4f, mixer.effectiveGain(AudioBus.SFX), 0.0001f);
    }
}
