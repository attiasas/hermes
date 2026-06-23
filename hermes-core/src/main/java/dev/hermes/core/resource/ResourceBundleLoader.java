package dev.hermes.core.resource;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Loads {@link ResourceBundle} instances from game asset paths. */
public final class ResourceBundleLoader {

    private ResourceBundleLoader() {
    }

    public static ResourceBundle load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new ResourceLoadException("resource bundle asset path is required");
        }
        FileHandle handle = HermesAssetPaths.internal(assetPath);
        if (!handle.exists()) {
            throw new ResourceLoadException("resource bundle not found: " + assetPath);
        }
        return parse(handle.readString(StandardCharsets.UTF_8.name()));
    }

    public static ResourceBundle loadById(String bundlesDirectory, String bundleId) {
        if (bundleId == null || bundleId.isBlank()) {
            throw new ResourceLoadException("bundle id is required");
        }
        String directory = bundlesDirectory == null || bundlesDirectory.isBlank()
                ? "resources/bundles"
                : bundlesDirectory.trim();
        if (directory.endsWith("/")) {
            directory = directory.substring(0, directory.length() - 1);
        }
        return load(directory + "/" + bundleId.trim() + ".json");
    }

    public static ResourceBundle parse(String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            int version = root.getInt("version", 0);
            if (version != 1) {
                throw new ResourceLoadException("\"version\" must be 1");
            }
            String id = requireString(root, "id", "resource bundle");
            JsonValue resourcesValue = root.get("resources");
            if (resourcesValue == null || !resourcesValue.isArray()) {
                throw new ResourceLoadException("\"resources\" array is required");
            }
            List<ResourceBundle.Entry> resources = new ArrayList<>();
            int index = 0;
            for (JsonValue entryValue : resourcesValue) {
                String context = "resource bundle entry [" + index + "]";
                if (!entryValue.isObject()) {
                    throw new ResourceLoadException(context + " must be an object");
                }
                String ref = requireString(entryValue, "ref", context);
                ResourceKind kind = parseKind(requireString(entryValue, "kind", context), context);
                resources.add(new ResourceBundle.Entry(ResourceRef.of(ref), kind));
                index++;
            }
            return ResourceBundle.of(version, id, resources);
        } catch (ResourceLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceLoadException("invalid resource bundle JSON: " + e.getMessage(), e);
        }
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
                throw new ResourceLoadException(context + ": unknown kind '" + kindName + "'");
        }
    }

    private static String requireString(JsonValue object, String field, String context) {
        if (!object.has(field)) {
            throw new ResourceLoadException(context + ": \"" + field + "\" is required");
        }
        String value = object.getString(field, "").trim();
        if (value.isEmpty()) {
            throw new ResourceLoadException(context + ": \"" + field + "\" must be non-empty");
        }
        return value;
    }
}
