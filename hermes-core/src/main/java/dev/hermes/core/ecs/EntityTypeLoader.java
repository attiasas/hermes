package dev.hermes.core.ecs;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.ecs.EntityTypeDefinition;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;

/** Loads {@link EntityTypeDefinition} instances from game asset paths. */
public final class EntityTypeLoader {

    private EntityTypeLoader() {
    }

    public static EntityTypeDefinition load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new SceneParseException("Entity type asset path is required");
        }
        FileHandle handle = HermesAssetPaths.internal(assetPath);
        if (!handle.exists()) {
            throw new SceneParseException("Entity type not found: " + assetPath);
        }
        return fromDocument(
                assetPath, EntityTypeDocument.parse(assetPath, handle.readString(StandardCharsets.UTF_8.name())));
    }

    static EntityTypeDefinition fromDocument(String assetPath, EntityTypeDocument document) {
        String expectedKind = EntityTypeDocument.kindFromPath(assetPath);
        if (!expectedKind.equals(document.kind())) {
            throw new SceneParseException(
                    "Entity type '" + assetPath + "': path kind '" + expectedKind + "' does not match parsed kind '"
                            + document.kind() + "'.");
        }
        return new EntityTypeDefinitionImpl(document.kind(), document.componentsJson());
    }
}
