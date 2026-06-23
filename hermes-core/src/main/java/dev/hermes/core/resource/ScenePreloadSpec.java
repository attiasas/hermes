package dev.hermes.core.resource;

import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.ecs.SceneParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Parsed scene JSON {@code preload} block. */
public final class ScenePreloadSpec {

    private final List<String> bundles;
    private final List<PathEntry> paths;
    private final boolean async;
    private final boolean showLoadingScreen;

    public ScenePreloadSpec(
            List<String> bundles, List<PathEntry> paths, boolean async, boolean showLoadingScreen) {
        this.bundles = bundles == null ? List.of() : List.copyOf(bundles);
        this.paths = paths == null ? List.of() : List.copyOf(paths);
        this.async = async;
        this.showLoadingScreen = showLoadingScreen;
    }

    public List<String> bundles() {
        return bundles;
    }

    public List<PathEntry> paths() {
        return paths;
    }

    public boolean async() {
        return async;
    }

    public boolean showLoadingScreen() {
        return showLoadingScreen;
    }

    public static ScenePreloadSpec parse(String scenePath, JsonValue preloadValue) {
        if (preloadValue == null || !preloadValue.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"preload\" must be an object.");
        }
        List<String> bundles = new ArrayList<>();
        JsonValue bundlesValue = preloadValue.get("bundles");
        if (bundlesValue != null) {
            if (!bundlesValue.isArray()) {
                throw new SceneParseException("Scene '" + scenePath + "': \"preload.bundles\" must be an array.");
            }
            for (int i = 0; i < bundlesValue.size; i++) {
                JsonValue entry = bundlesValue.get(i);
                if (entry == null || !entry.isString()) {
                    throw new SceneParseException(
                            "Scene '" + scenePath + "': \"preload.bundles[" + i + "]\" must be a string.");
                }
                String bundleId = entry.asString().trim();
                if (bundleId.isEmpty()) {
                    throw new SceneParseException(
                            "Scene '" + scenePath + "': \"preload.bundles[" + i + "]\" must be non-empty.");
                }
                bundles.add(bundleId);
            }
        }
        List<PathEntry> paths = new ArrayList<>();
        JsonValue pathsValue = preloadValue.get("paths");
        if (pathsValue != null) {
            if (!pathsValue.isArray()) {
                throw new SceneParseException("Scene '" + scenePath + "': \"preload.paths\" must be an array.");
            }
            for (int i = 0; i < pathsValue.size; i++) {
                JsonValue entry = pathsValue.get(i);
                String context = "Scene '" + scenePath + "': \"preload.paths[" + i + "]\"";
                if (entry == null || !entry.isObject()) {
                    throw new SceneParseException(context + " must be an object.");
                }
                String ref = requireString(entry, "ref", context);
                ResourceKind kind = parseKind(requireString(entry, "kind", context), context);
                paths.add(new PathEntry(ResourceRef.of(ref), kind));
            }
        }
        boolean async = preloadValue.getBoolean("async", false);
        boolean showLoadingScreen = preloadValue.getBoolean("showLoadingScreen", true);
        return new ScenePreloadSpec(bundles, paths, async, showLoadingScreen);
    }

    private static ResourceKind parseKind(String kindName, String context) {
        String normalized = kindName.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "texture":
                return ResourceKind.TEXTURE;
            case "model":
                return ResourceKind.MODEL;
            case "gltf_model":
            case "gltf-model":
            case "gltfmodel":
                return ResourceKind.GLTF_MODEL;
            case "sound":
                return ResourceKind.SOUND;
            case "font":
                return ResourceKind.FONT;
            case "json":
                return ResourceKind.JSON;
            case "binary":
                return ResourceKind.BINARY;
            case "animation_clip":
            case "animation-clip":
            case "animationclip":
                return ResourceKind.ANIMATION_CLIP;
            default:
                throw new SceneParseException(context + ": unknown kind '" + kindName + "'");
        }
    }

    private static String requireString(JsonValue object, String field, String context) {
        if (!object.has(field)) {
            throw new SceneParseException(context + ": \"" + field + "\" is required.");
        }
        String value = object.getString(field, "").trim();
        if (value.isEmpty()) {
            throw new SceneParseException(context + ": \"" + field + "\" must be non-empty.");
        }
        return value;
    }

    public static final class PathEntry {
        private final ResourceRef ref;
        private final ResourceKind kind;

        public PathEntry(ResourceRef ref, ResourceKind kind) {
            this.ref = Objects.requireNonNull(ref, "ref");
            this.kind = Objects.requireNonNull(kind, "kind");
        }

        public ResourceRef ref() {
            return ref;
        }

        public ResourceKind kind() {
            return kind;
        }
    }
}
