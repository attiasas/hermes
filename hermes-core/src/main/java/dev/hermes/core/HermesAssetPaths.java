package dev.hermes.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Resolves game asset paths for dev (working dir) and packaged (classpath {@code assets/}) layouts.
 */
public final class HermesAssetPaths {

    private static final String CLASSPATH_PREFIX = "assets/";

    private HermesAssetPaths() {
    }

    /**
     * Returns an internal file handle for a game asset path, trying {@code assets/<path>} when needed.
     */
    public static FileHandle internal(String path) {
        if (path == null || path.isBlank()) {
            return Gdx.files.internal("");
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        FileHandle handle = Gdx.files.internal(normalized);
        if (handle.exists()) {
            return handle;
        }
        if (!normalized.startsWith(CLASSPATH_PREFIX)) {
            FileHandle prefixed = Gdx.files.internal(CLASSPATH_PREFIX + normalized);
            if (prefixed.exists()) {
                return prefixed;
            }
        }
        return handle;
    }
}
