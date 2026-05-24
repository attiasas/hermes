package dev.hermes.core.scene;

import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.api.scene.SceneSource;
import dev.hermes.core.ecs.SceneLoader;

/**
 * Loads scene content from a JSON asset via {@link SceneLoader}.
 */
public final class AssetSceneSource implements SceneSource {

    private final String assetPath;

    public AssetSceneSource(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new IllegalArgumentException("assetPath is required");
        }
        this.assetPath = assetPath;
    }

    public String assetPath() {
        return assetPath;
    }

    @Override
    public void populate(SceneLoadContext ctx) {
        SceneLoader.load(assetPath, ctx);
    }
}
