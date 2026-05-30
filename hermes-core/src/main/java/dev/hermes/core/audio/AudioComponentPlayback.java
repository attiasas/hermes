package dev.hermes.core.audio;

import dev.hermes.api.audio.ClipId;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.api.audio.SoundHandle;

/** Shared clip playback for ECS audio components. */
final class AudioComponentPlayback {

    private AudioComponentPlayback() {}

    static SoundHandle play(
            AudioServiceImpl audio, String clip, boolean clipIsId, PlayOptions options) {
        if (clip == null || clip.isBlank()) {
            return null;
        }
        if (clipIsId) {
            return audio.play(ClipId.of(clip), options);
        }
        return audio.play(clip, options);
    }
}
