package dev.hermes.core.resource.loaders;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.animation.AnimationClipLoader;
import dev.hermes.core.resource.DecodedPayload;
import dev.hermes.core.resource.ResourceLoader;

import java.nio.charset.StandardCharsets;

/** Loads Hermes animation clip JSON documents. */
public final class AnimationClipResourceLoader implements ResourceLoader {

    private final AnimationClipLoader loader = new AnimationClipLoader();

    @Override
    public ResourceKind kind() {
        return ResourceKind.ANIMATION_CLIP;
    }

    @Override
    public DecodedPayload decode(String path) {
        FileHandle file = HermesAssetPaths.internal(path);
        if (!file.exists()) {
            throw new ResourceLoadException("Animation clip asset not found: " + path);
        }
        return DecodedPayload.fromSourcePath(path);
    }

    @Override
    public Object upload(DecodedPayload decoded) {
        String path = decoded.sourcePath();
        if (path == null || path.isBlank()) {
            throw new ResourceLoadException("Animation clip decode produced no source path");
        }
        FileHandle file = HermesAssetPaths.internal(path);
        if (!file.exists()) {
            throw new ResourceLoadException("Animation clip asset not found: " + path);
        }
        return loader.parse(file.readString(StandardCharsets.UTF_8.name()));
    }

    @Override
    public void dispose(Object resource) {
        // Immutable POJO; nothing to dispose.
    }
}
