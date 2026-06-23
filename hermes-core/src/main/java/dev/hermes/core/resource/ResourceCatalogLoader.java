package dev.hermes.core.resource;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Loads {@link ResourceCatalog} instances from game asset paths. */
public final class ResourceCatalogLoader {

    private ResourceCatalogLoader() {
    }

    public static ResourceCatalog load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new ResourceLoadException("resource catalog asset path is required");
        }
        FileHandle handle = HermesAssetPaths.internal(assetPath);
        if (!handle.exists()) {
            throw new ResourceLoadException("resource catalog not found: " + assetPath);
        }
        return parse(handle.readString(StandardCharsets.UTF_8.name()));
    }

    public static ResourceCatalog parse(String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            int version = root.getInt("version", 0);
            if (version != 1) {
                throw new ResourceLoadException("\"version\" must be 1");
            }
            JsonValue entriesValue = root.get("entries");
            if (entriesValue == null || !entriesValue.isObject()) {
                throw new ResourceLoadException("\"entries\" object is required");
            }
            Map<String, ResourceCatalog.Entry> entries = new HashMap<>();
            for (JsonValue entryValue : entriesValue) {
                String alias = entryValue.name;
                if (alias == null || alias.isBlank()) {
                    throw new ResourceLoadException("catalog entry name is required");
                }
                if (!alias.startsWith("@")) {
                    throw new ResourceLoadException("catalog entry '" + alias + "' must be an @alias");
                }
                if (!entryValue.isObject()) {
                    throw new ResourceLoadException("catalog entry '" + alias + "' must be an object");
                }
                String path = requireString(entryValue, "path", "catalog entry '" + alias + "'");
                ResourceKind kind = parseKind(requireString(entryValue, "kind", "catalog entry '" + alias + "'"), alias);
                entries.put(alias, new ResourceCatalog.Entry(path, kind));
            }
            return ResourceCatalog.of(version, entries);
        } catch (ResourceLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceLoadException("invalid resource catalog JSON: " + e.getMessage(), e);
        }
    }

    private static ResourceKind parseKind(String kindName, String alias) {
        String normalized = kindName.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "texture":
                return ResourceKind.TEXTURE;
            case "model":
                return ResourceKind.MODEL;
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
                throw new ResourceLoadException(
                        "catalog entry '" + alias + "': unknown kind '" + kindName + "'");
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
