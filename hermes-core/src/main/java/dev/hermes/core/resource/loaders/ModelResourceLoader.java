package dev.hermes.core.resource.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.render.resource.PrimitiveModelDocument;
import dev.hermes.core.render.resource.PrimitiveModelGenerator;
import dev.hermes.core.resource.DecodedPayload;
import dev.hermes.core.resource.ResourceLoader;

/** Loads Wavefront OBJ models or procedural primitives from generator JSON / synthetic paths. */
public final class ModelResourceLoader implements ResourceLoader {

    private final ObjLoader objLoader = new ObjLoader();
    private final PrimitiveModelGenerator primitiveGenerator = new PrimitiveModelGenerator();

    @Override
    public ResourceKind kind() {
        return ResourceKind.MODEL;
    }

    @Override
    public DecodedPayload decode(String path) {
        if (PrimitiveModelDocument.isSyntheticPath(path)) {
            return DecodedPayload.fromSourcePath(path);
        }
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
        if (PrimitiveModelDocument.isSyntheticPath(path)) {
            return primitiveGenerator.generate(PrimitiveModelDocument.parseSyntheticPath(path));
        }
        FileHandle file = HermesAssetPaths.internal(path);
        if (path.endsWith(".json")) {
            return primitiveGenerator.generate(PrimitiveModelDocument.parseJson(file.readString("UTF-8")));
        }
        return objLoader.loadModel(file);
    }

    @Override
    public void dispose(Object resource) {
        if (resource instanceof Model) {
            ((Model) resource).dispose();
        }
    }
}
