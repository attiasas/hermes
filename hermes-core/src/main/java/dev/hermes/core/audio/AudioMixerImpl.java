package dev.hermes.core.audio;

import dev.hermes.api.audio.AudioBus;
import dev.hermes.api.audio.AudioMixer;

import java.util.EnumMap;
import java.util.Map;

/** Session-scoped bus volume control with master * bus effective gain. */
public final class AudioMixerImpl implements AudioMixer {

    private final Map<AudioBus, Float> volumes = new EnumMap<>(AudioBus.class);

    public AudioMixerImpl() {
        for (AudioBus bus : AudioBus.values()) {
            volumes.put(bus, 1f);
        }
    }

    @Override
    public float volume(AudioBus bus) {
        return volumes.get(bus);
    }

    @Override
    public void setVolume(AudioBus bus, float volume01) {
        volumes.put(bus, Math.max(0f, Math.min(1f, volume01)));
    }

    @Override
    public float effectiveGain(AudioBus bus) {
        if (bus == AudioBus.MASTER) {
            return volume(AudioBus.MASTER);
        }
        return volume(AudioBus.MASTER) * volume(bus);
    }
}
