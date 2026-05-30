package dev.hermes.core.audio;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Parsed BGM playlist from {@code audio/bgm/*.json}. */
final class BgmPlaylist {

    enum Mode {
        SEQUENTIAL,
        RANDOM,
        SINGLE
    }

    private static final float DEFAULT_CROSSFADE_SECONDS = 2f;

    private final Mode mode;
    private final List<String> tracks;
    private final float crossfadeSeconds;

    BgmPlaylist(Mode mode, List<String> tracks, float crossfadeSeconds) {
        this.mode = mode;
        this.tracks = Collections.unmodifiableList(new ArrayList<>(tracks));
        this.crossfadeSeconds = crossfadeSeconds;
    }

    Mode mode() {
        return mode;
    }

    List<String> tracks() {
        return tracks;
    }

    float crossfadeSeconds() {
        return crossfadeSeconds;
    }

    static BgmPlaylist load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new AudioProfileParseException("BGM playlist asset path is required");
        }
        if (!HermesAssetPaths.internal(assetPath).exists()) {
            throw new AudioProfileParseException("BGM playlist not found: " + assetPath);
        }
        return parse(HermesAssetPaths.internal(assetPath).readString(StandardCharsets.UTF_8.name()));
    }

    static BgmPlaylist parse(String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            int version = root.getInt("version", 0);
            if (version < 1) {
                throw new AudioProfileParseException("BGM playlist \"version\" must be >= 1");
            }
            Mode mode = parseMode(root.getString("mode", "sequential"));
            List<String> tracks = parseTracks(root.get("tracks"));
            float crossfadeSeconds = root.getFloat("crossfadeSeconds", DEFAULT_CROSSFADE_SECONDS);
            return new BgmPlaylist(mode, tracks, crossfadeSeconds);
        } catch (AudioProfileParseException e) {
            throw e;
        } catch (Exception e) {
            throw new AudioProfileParseException("invalid BGM playlist JSON: " + e.getMessage(), e);
        }
    }

    private static Mode parseMode(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "random":
                return Mode.RANDOM;
            case "single":
                return Mode.SINGLE;
            case "sequential":
                return Mode.SEQUENTIAL;
            default:
                throw new AudioProfileParseException("unknown BGM playlist mode '" + raw + "'");
        }
    }

    private static List<String> parseTracks(JsonValue tracksValue) {
        if (tracksValue == null || !tracksValue.isArray() || tracksValue.size == 0) {
            throw new AudioProfileParseException("BGM playlist \"tracks\" array is required");
        }
        List<String> tracks = new ArrayList<>();
        for (int i = 0; i < tracksValue.size; i++) {
            String track = tracksValue.get(i).asString().trim();
            if (track.isEmpty()) {
                throw new AudioProfileParseException("BGM playlist tracks[" + i + "] must be non-empty");
            }
            tracks.add(track);
        }
        return tracks;
    }
}
