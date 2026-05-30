package dev.hermes.core.ecs;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.ecs.EntityTypeDefinition;
import dev.hermes.api.ecs.EntityTypeRegistry;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.core.HermesAssetPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory registry of entity type templates loaded from {@code entities/<kind>/type.json}.
 */
public final class EntityTypeRegistryImpl implements EntityTypeRegistry {

    private static final Logger log = Logs.get(EntityTypeRegistryImpl.class);

    private final Map<String, EntityTypeDefinition> definitions = new LinkedHashMap<>();

    @Override
    public void scanAssets() {
        FileHandle entitiesDir = HermesAssetPaths.internal("entities/");
        if (!entitiesDir.exists() || !entitiesDir.isDirectory()) {
            return;
        }
        for (FileHandle kindDir : entitiesDir.list()) {
            if (!kindDir.isDirectory()) {
                continue;
            }
            FileHandle typeJson = kindDir.child("type.json");
            if (typeJson.exists()) {
                register(kindDir.name(), toAssetPath(kindDir.name()));
            }
        }
    }

    @Override
    public void register(String kind, String assetPath) {
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("Entity type kind is required");
        }
        if (definitions.containsKey(kind)) {
            throw new IllegalArgumentException("Entity type '" + kind + "' is already registered");
        }
        EntityTypeDefinition definition = EntityTypeLoader.load(assetPath);
        if (!kind.equals(definition.kind())) {
            throw new IllegalArgumentException(
                    "Entity type kind '" + kind + "' does not match asset path kind '" + definition.kind() + "'");
        }
        log.debug("Registering entity type: " + kind + " from " + assetPath);
        definitions.put(kind, definition);
    }

    @Override
    public boolean has(String kind) {
        return definitions.containsKey(kind);
    }

    @Override
    public EntityTypeDefinition require(String kind) {
        EntityTypeDefinition definition = definitions.get(kind);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown entity type: " + kind);
        }
        return definition;
    }

    void scanTestAssets(String assetPath) {
        String json = readClasspathResource(assetPath);
        EntityTypeDocument document = EntityTypeDocument.parse(assetPath, json);
        EntityTypeDefinition definition = EntityTypeLoader.fromDocument(assetPath, document);
        definitions.put(definition.kind(), definition);
    }

    private static String toAssetPath(String kind) {
        return "entities/" + kind + "/type.json";
    }

    private static String readClasspathResource(String assetPath) {
        String normalized = assetPath.startsWith("/") ? assetPath.substring(1) : assetPath;
        ClassLoader loader = EntityTypeRegistryImpl.class.getClassLoader();
        try (InputStream in = openClasspathResource(loader, normalized)) {
            if (in == null) {
                throw new IllegalArgumentException("Test asset not found: " + assetPath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read test asset: " + assetPath, e);
        }
    }

    private static InputStream openClasspathResource(ClassLoader loader, String path) {
        InputStream in = loader.getResourceAsStream(path);
        if (in != null) {
            return in;
        }
        return loader.getResourceAsStream("assets/" + path);
    }
}
