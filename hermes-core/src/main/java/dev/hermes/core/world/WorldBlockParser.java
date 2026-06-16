package dev.hermes.core.world;

import dev.hermes.api.scene.SceneUiConfig;
import dev.hermes.api.world.WorldBounds;
import dev.hermes.api.world.WorldKind;
import dev.hermes.core.ecs.SceneParseException;

import com.badlogic.gdx.utils.JsonValue;

import java.util.Optional;

/** Parses scene JSON {@code "world"} block (version 1). */
public final class WorldBlockParser {

    private WorldBlockParser() {}

    public static WorldBlock parse(String scenePath, JsonValue worldValue, Optional<SceneUiConfig> uiConfig) {
        if (worldValue == null || worldValue.isNull()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"world\" is required for parse.");
        }
        if (!worldValue.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"world\" must be an object.");
        }
        int version = worldValue.getInt("version", -1);
        if (version != 1) {
            throw new SceneParseException("Scene '" + scenePath + "': \"world.version\" must be 1.");
        }
        WorldKind kind = parseKind(scenePath, worldValue.getString("kind", "open"));
        Optional<String> tilemapPath = Optional.empty();
        if (worldValue.has("tilemap")) {
            String path = worldValue.getString("tilemap", "").trim();
            if (path.isEmpty()) {
                throw new SceneParseException("Scene '" + scenePath + "': \"world.tilemap\" must be non-empty.");
            }
            tilemapPath = Optional.of(path);
        }
        if (kind == WorldKind.TILEMAP && tilemapPath.isEmpty()) {
            throw new SceneParseException(
                    "Scene '" + scenePath + "': \"world.tilemap\" is required when \"world.kind\" is \"tilemap\".");
        }
        WorldBounds bounds = WorldBoundsResolver.resolve(scenePath, worldValue.get("dimensions"), uiConfig);
        String spatialStrategy = "bruteForce";
        float cellSize = 128f;
        JsonValue spatial = worldValue.get("spatial");
        if (spatial != null && spatial.isObject()) {
            spatialStrategy = spatial.getString("strategy", spatialStrategy).trim();
            if (spatialStrategy.isEmpty()) {
                spatialStrategy = "bruteForce";
            }
            cellSize = spatial.getFloat("cellSize", cellSize);
        }
        if (kind == WorldKind.TILEMAP && "bruteForce".equals(spatialStrategy)) {
            spatialStrategy = "tilemap";
        }
        return new WorldBlock(kind, bounds, spatialStrategy, cellSize, tilemapPath);
    }

    public static WorldBlock parse(String scenePath, JsonValue worldValue) {
        return parse(scenePath, worldValue, Optional.empty());
    }

    private static WorldKind parseKind(String scenePath, String kindName) {
        if (kindName == null || kindName.isBlank() || "open".equalsIgnoreCase(kindName)) {
            return WorldKind.OPEN;
        }
        if ("tilemap".equalsIgnoreCase(kindName)) {
            return WorldKind.TILEMAP;
        }
        throw new SceneParseException(
                "Scene '" + scenePath + "': \"world.kind\" must be \"open\" or \"tilemap\".");
    }
}
