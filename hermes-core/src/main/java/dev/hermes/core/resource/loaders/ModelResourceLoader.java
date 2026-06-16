package dev.hermes.core.resource.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.resource.DecodedPayload;
import dev.hermes.core.resource.ResourceLoader;

/** Loads Wavefront OBJ models via {@link ObjLoader}. */
public final class ModelResourceLoader implements ResourceLoader {

    private final ObjLoader objLoader = new ObjLoader();

    @Override
    public ResourceKind kind() {
        return ResourceKind.MODEL;
    }

    @Override
    public DecodedPayload decode(String path) {
        FileHandle file = HermesAssetPaths.internal(path);
        if (!file.exists()) {
            throw new ResourceLoadException("Model asset not found: " + path);
        }
        return DecodedPayload.fromSourcePath(path);
    }

    @Override
    public Object upload(DecodedPayload decoded) {
        String path = decoded.sourcePath();
        if (path == null || path.isBlank()) {
            throw new ResourceLoadException("Model decode produced no source path");
        }
        return objLoader.loadModel(HermesAssetPaths.internal(path));
    }

    @Override
    public void dispose(Object resource) {
        if (resource instanceof Model) {
            ((Model) resource).dispose();
        }
    }
}
