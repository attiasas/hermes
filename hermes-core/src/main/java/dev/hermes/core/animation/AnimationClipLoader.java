package dev.hermes.core.animation;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.animation.AnimationClip;
import dev.hermes.api.animation.AnimationTrack;
import dev.hermes.api.animation.Interpolation;
import dev.hermes.api.animation.Keyframe;
import dev.hermes.api.resource.ResourceLoadException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Parses Hermes animation clip JSON documents. */
public final class AnimationClipLoader {

    public AnimationClip parse(String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            if (root == null || !root.isObject()) {
                throw new ResourceLoadException("Animation clip root must be an object");
            }
            int version = root.getInt("version", 0);
            if (version != 1) {
                throw new ResourceLoadException("Animation clip \"version\" must be 1");
            }
            float duration = requireNumber(root, "duration", "animation clip");
            boolean loop = requireBoolean(root, "loop", "animation clip");
            JsonValue tracksValue = requireArray(root, "tracks", "animation clip");
            List<AnimationTrack> tracks = new ArrayList<>();
            int trackIndex = 0;
            for (JsonValue trackValue : tracksValue) {
                String context = "track[" + trackIndex + "]";
                if (!trackValue.isObject()) {
                    throw new ResourceLoadException(context + " must be an object");
                }
                tracks.add(parseTrack(trackValue, context));
                trackIndex++;
            }
            return new AnimationClip(version, duration, loop, tracks);
        } catch (ResourceLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceLoadException("Invalid animation clip JSON: " + e.getMessage(), e);
        }
    }

    private static AnimationTrack parseTrack(JsonValue trackValue, String context) {
        String target = requireString(trackValue, "target", context);
        Interpolation interpolation =
                parseInterpolation(requireString(trackValue, "interpolation", context), context);
        JsonValue keyframesValue = requireArray(trackValue, "keyframes", context);
        List<Keyframe> keyframes = new ArrayList<>();
        int keyframeIndex = 0;
        for (JsonValue keyframeValue : keyframesValue) {
            String keyframeContext = context + ".keyframes[" + keyframeIndex + "]";
            if (!keyframeValue.isObject()) {
                throw new ResourceLoadException(keyframeContext + " must be an object");
            }
            keyframes.add(parseKeyframe(keyframeValue, keyframeContext));
            keyframeIndex++;
        }
        return new AnimationTrack(target, interpolation, keyframes);
    }

    private static Keyframe parseKeyframe(JsonValue keyframeValue, String context) {
        float t = requireNumber(keyframeValue, "t", context);
        JsonValue vValue = keyframeValue.get("v");
        if (vValue == null) {
            throw new ResourceLoadException(context + ": \"v\" is required");
        }
        if (vValue.isNumber()) {
            return new Keyframe(t, vValue.asFloat());
        }
        if (vValue.isArray()) {
            if (vValue.size == 0) {
                throw new ResourceLoadException(context + ": \"v\" array must be non-empty");
            }
            float[] values = new float[vValue.size];
            for (int i = 0; i < vValue.size; i++) {
                JsonValue entry = vValue.get(i);
                if (entry == null || !entry.isNumber()) {
                    throw new ResourceLoadException(context + ": \"v[" + i + "]\" must be a number");
                }
                values[i] = entry.asFloat();
            }
            return new Keyframe(t, values);
        }
        throw new ResourceLoadException(context + ": \"v\" must be a number or number array");
    }

    private static Interpolation parseInterpolation(String interpolationName, String context) {
        String normalized = interpolationName.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "step":
                return Interpolation.STEP;
            case "linear":
                return Interpolation.LINEAR;
            default:
                throw new ResourceLoadException(
                        context + ": unsupported interpolation '" + interpolationName + "'");
        }
    }

    private static JsonValue requireArray(JsonValue parent, String field, String context) {
        JsonValue value = parent.get(field);
        if (value == null || !value.isArray()) {
            throw new ResourceLoadException(context + ": \"" + field + "\" must be an array");
        }
        return value;
    }

    private static String requireString(JsonValue object, String field, String context) {
        JsonValue value = object.get(field);
        if (value == null || !value.isString()) {
            throw new ResourceLoadException(context + ": \"" + field + "\" must be a string");
        }
        String text = value.asString().trim();
        if (text.isEmpty()) {
            throw new ResourceLoadException(context + ": \"" + field + "\" must be non-empty");
        }
        return text;
    }

    private static float requireNumber(JsonValue object, String field, String context) {
        JsonValue value = object.get(field);
        if (value == null || !value.isNumber()) {
            throw new ResourceLoadException(context + ": \"" + field + "\" must be a number");
        }
        return value.asFloat();
    }

    private static boolean requireBoolean(JsonValue object, String field, String context) {
        JsonValue value = object.get(field);
        if (value == null || !value.isBoolean()) {
            throw new ResourceLoadException(context + ": \"" + field + "\" must be a boolean");
        }
        return value.asBoolean();
    }
}
