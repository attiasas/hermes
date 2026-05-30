package dev.hermes.core.audio;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.audio.AudioBus;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Loads {@link AudioProfile} instances from game asset paths. */
public final class AudioProfileLoader {

    private static final int DEFAULT_MAX_INSTANCES_PER_CLIP = 8;

    private AudioProfileLoader() {
    }

    public static AudioProfile load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new AudioProfileParseException("audio profile asset path is required");
        }
        if (!HermesAssetPaths.internal(assetPath).exists()) {
            throw new AudioProfileParseException("audio profile not found: " + assetPath);
        }
        return parse(HermesAssetPaths.internal(assetPath).readString(StandardCharsets.UTF_8.name()));
    }

    public static AudioProfile parse(String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            int version = root.getInt("version", 0);
            if (version < 1) {
                throw new AudioProfileParseException("\"version\" must be >= 1");
            }

            Map<String, String> clips = parseStringMap(root.get("clips"), "clips");
            Map<String, String> actionSounds = parseStringMap(root.get("actionSounds"), "actionSounds");
            Map<AudioBus, Float> busVolumes = parseBuses(root.get("buses"));
            int maxInstances = DEFAULT_MAX_INSTANCES_PER_CLIP;
            JsonValue limits = root.get("limits");
            if (limits != null && limits.isObject()) {
                maxInstances = limits.getInt("maxInstancesPerClip", DEFAULT_MAX_INSTANCES_PER_CLIP);
            }
            return new AudioProfile(clips, actionSounds, busVolumes, maxInstances);
        } catch (AudioProfileParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AudioProfileParseException("invalid audio profile JSON: " + e.getMessage(), e);
        }
    }

    private static Map<String, String> parseStringMap(JsonValue value, String fieldName) {
        Map<String, String> map = new HashMap<>();
        if (value == null || !value.isObject()) {
            return map;
        }
        for (JsonValue entry : value) {
            if (entry.name == null || entry.name.isBlank()) {
                throw new AudioProfileParseException("\"" + fieldName + "\" entry name is required");
            }
            String path = entry.asString();
            if (path == null || path.isBlank()) {
                throw new AudioProfileParseException(
                        "\"" + fieldName + "\" entry '" + entry.name + "' must be non-empty");
            }
            map.put(entry.name, path);
        }
        return map;
    }

    private static Map<AudioBus, Float> parseBuses(JsonValue busesValue) {
        Map<AudioBus, Float> busVolumes = new EnumMap<>(AudioBus.class);
        if (busesValue == null || !busesValue.isObject()) {
            return busVolumes;
        }
        for (JsonValue entry : busesValue) {
            if (entry.name == null || entry.name.isBlank()) {
                continue;
            }
            AudioBus bus = parseBusName(entry.name);
            if (bus != null) {
                busVolumes.put(bus, entry.asFloat());
            }
        }
        return busVolumes;
    }

    private static AudioBus parseBusName(String name) {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "master":
                return AudioBus.MASTER;
            case "sfx":
                return AudioBus.SFX;
            case "music":
                return AudioBus.MUSIC;
            case "ambient":
                return AudioBus.AMBIENT;
            default:
                return null;
        }
    }
}
