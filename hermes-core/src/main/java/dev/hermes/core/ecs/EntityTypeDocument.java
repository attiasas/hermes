package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Parses entity type JSON from {@code entities/<kind>/type.json}.
 */
final class EntityTypeDocument {

    private static final String PATH_PREFIX = "entities/";
    private static final String PATH_SUFFIX = "/type.json";

    private final int version;
    private final String kind;
    private final JsonValue componentsJson;

    private EntityTypeDocument(int version, String kind, JsonValue componentsJson) {
        this.version = version;
        this.kind = kind;
        this.componentsJson = componentsJson;
    }

    static EntityTypeDocument parse(String path, String json) {
        String kind = kindFromPath(path);
        try {
            JsonValue root = new JsonReader().parse(json);
            int version = root.getInt("version", 0);
            if (version != 1) {
                throw new SceneParseException(
                        "Entity type '" + path + "': \"version\" must be 1.");
            }
            JsonValue components = root.get("components");
            if (components == null || !components.isObject()) {
                throw new SceneParseException(
                        "Entity type '" + path + "': \"components\" object is required.");
            }
            return new EntityTypeDocument(version, kind, components);
        } catch (SceneParseException e) {
            throw e;
        } catch (Exception e) {
            throw new SceneParseException(
                    "Entity type '" + path + "': invalid JSON: " + e.getMessage(), e);
        }
    }

    static String kindFromPath(String path) {
        String normalized = normalizePath(path);
        if (!normalized.startsWith(PATH_PREFIX) || !normalized.endsWith(PATH_SUFFIX)) {
            throw new SceneParseException(
                    "Entity type path must be entities/<kind>/type.json: " + path);
        }
        String kind = normalized.substring(PATH_PREFIX.length(), normalized.length() - PATH_SUFFIX.length());
        if (kind.isEmpty() || kind.contains("/")) {
            throw new SceneParseException(
                    "Entity type path must be entities/<kind>/type.json: " + path);
        }
        return kind;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String normalized = path.replace('\\', '/');
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("assets/")) {
            normalized = normalized.substring("assets/".length());
        }
        return normalized;
    }

    int version() {
        return version;
    }

    String kind() {
        return kind;
    }

    JsonValue componentsJson() {
        return componentsJson;
    }
}
