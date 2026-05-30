package dev.hermes.api;

import dev.hermes.api.audio.AudioMixer;

/**
 * Per-game session state shared across scenes (player profile, settings, progression).
 *
 * <p>Future: save slots and cross-scene services will live here.
 */
public interface HermesSession {

    /** Session-scoped mixer; same instance for app lifetime. */
    AudioMixer mixer();

    /**
     * No-op session used until session services are implemented.
     */
    HermesSession EMPTY =
            new HermesSession() {
                @Override
                public AudioMixer mixer() {
                    return AudioMixer.NOOP;
                }
            };
}
