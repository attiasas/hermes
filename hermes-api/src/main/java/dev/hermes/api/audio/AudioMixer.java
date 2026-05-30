package dev.hermes.api.audio;

/** Session-scoped bus volume control. */
public interface AudioMixer {

    AudioMixer NOOP =
            new AudioMixer() {
                @Override
                public float volume(AudioBus bus) {
                    return 1f;
                }

                @Override
                public void setVolume(AudioBus bus, float volume01) {}

                @Override
                public float effectiveGain(AudioBus bus) {
                    return 1f;
                }
            };

    float volume(AudioBus bus);

    void setVolume(AudioBus bus, float volume01);

    /** Effective multiplier applied to plays on this bus (master * bus). */
    float effectiveGain(AudioBus bus);
}
