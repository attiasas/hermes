package dev.hermes.core.render;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;

/**
 * Loads {@link PipelineDocument} instances from game or engine asset paths.
 */
public final class PipelineLoader {

    private static final String BUILTIN_FORWARD = "render/builtin-forward.json";

    private PipelineLoader() {
    }

    public static PipelineDocument load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new PipelineParseException("render pipeline asset path is required");
        }
        FileHandle handle = HermesAssetPaths.internal(assetPath);
        if (!handle.exists()) {
            throw new PipelineParseException("render pipeline not found: " + assetPath);
        }
        return PipelineDocument.parse(handle.readString(StandardCharsets.UTF_8.name()));
    }

    public static PipelineDocument loadBuiltin() {
        return load(BUILTIN_FORWARD);
    }
}
