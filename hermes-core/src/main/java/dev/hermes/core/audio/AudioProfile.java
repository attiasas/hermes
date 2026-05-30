package dev.hermes.core.audio;

import dev.hermes.api.audio.AudioBus;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/** Parsed audio profile data from {@code audio/profile.json}. */
final class AudioProfile {

    private static final int DEFAULT_MAX_INSTANCES_PER_CLIP = 8;

    private final Map<String, String> clips;
    private final Map<String, String> actionSounds;
    private final Map<AudioBus, Float> busVolumes;
    private final int maxInstancesPerClip;

    AudioProfile(
            Map<String, String> clips,
            Map<String, String> actionSounds,
            Map<AudioBus, Float> busVolumes,
            int maxInstancesPerClip) {
        this.clips = Collections.unmodifiableMap(clips);
        this.actionSounds = Collections.unmodifiableMap(actionSounds);
        this.busVolumes = Collections.unmodifiableMap(busVolumes);
        this.maxInstancesPerClip =
                maxInstancesPerClip > 0 ? maxInstancesPerClip : DEFAULT_MAX_INSTANCES_PER_CLIP;
    }

    Optional<String> resolveClip(String clipId) {
        return Optional.ofNullable(clips.get(clipId));
    }

    float busVolume(AudioBus bus) {
        return busVolumes.getOrDefault(bus, 1f);
    }

    Optional<String> actionSoundClipId(String action) {
        return Optional.ofNullable(actionSounds.get(action));
    }

    int maxInstancesPerClip() {
        return maxInstancesPerClip;
    }

    Iterable<Map.Entry<String, String>> actionSoundEntries() {
        return actionSounds.entrySet();
    }
}
