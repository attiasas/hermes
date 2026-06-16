package dev.hermes.core.resource;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;

/** Loads {@link ResourceProfile} instances from game asset paths. */
public final class ResourceProfileLoader {

    private ResourceProfileLoader() {
    }

    public static ResourceProfile load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new ResourceLoadException("resource profile asset path is required");
        }
        FileHandle handle = HermesAssetPaths.internal(assetPath);
        if (!handle.exists()) {
            throw new ResourceLoadException("resource profile not found: " + assetPath);
        }
        return parse(handle.readString(StandardCharsets.UTF_8.name()));
    }

    public static ResourceProfile parse(String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            int version = root.getInt("version", 0);
            if (version != 1) {
                throw new ResourceLoadException("\"version\" must be 1");
            }
            ResourceProfile defaults = ResourceProfile.defaults();
            String catalog = optionalString(root, "catalog", defaults.catalog());
            String bundlesDirectory =
                    optionalString(root, "bundlesDirectory", defaults.bundlesDirectory());
            boolean defaultAsync = root.getBoolean("defaultAsync", defaults.defaultAsync());
            boolean htmlDefaultAsync = root.getBoolean("htmlDefaultAsync", defaults.htmlDefaultAsync());
            int cooperativeAssetsPerFrame =
                    root.getInt("cooperativeAssetsPerFrame", defaults.cooperativeAssetsPerFrame());
            if (cooperativeAssetsPerFrame < 1) {
                throw new ResourceLoadException("\"cooperativeAssetsPerFrame\" must be >= 1");
            }
            boolean showLoadingScreenWhenAsync =
                    root.getBoolean("showLoadingScreenWhenAsync", defaults.showLoadingScreenWhenAsync());
            return new ResourceProfile(
                    version,
                    catalog,
                    bundlesDirectory,
                    defaultAsync,
                    htmlDefaultAsync,
                    cooperativeAssetsPerFrame,
                    showLoadingScreenWhenAsync);
        } catch (ResourceLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceLoadException("invalid resource profile JSON: " + e.getMessage(), e);
        }
    }

    private static String optionalString(JsonValue object, String field, String defaultValue) {
        if (!object.has(field)) {
            return defaultValue;
        }
        String value = object.getString(field, "").trim();
        return value.isEmpty() ? defaultValue : value;
    }
}
