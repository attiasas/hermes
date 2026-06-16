package dev.hermes.core.world.tilemap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.resource.DecodedPayload;
import dev.hermes.core.resource.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads Hermes `.hmap.json` tilemap assets. */
public final class HermesTileMapLoader implements ResourceLoader {

    @Override
    public ResourceKind kind() {
        return ResourceKind.TILEMAP;
    }

    @Override
    public DecodedPayload decode(String path) {
        FileHandle file = HermesAssetPaths.internal(path);
        if (!file.exists()) {
            throw new ResourceLoadException("Tilemap asset not found: " + path);
        }
        try (InputStream in = file.read()) {
            return DecodedPayload.fromBytes(in.readAllBytes());
        } catch (IOException e) {
            throw new ResourceLoadException("Failed to read tilemap: " + path, e);
        }
    }

    @Override
    public Object upload(DecodedPayload decoded) {
        byte[] bytes = decoded.bytes();
        if (bytes == null || bytes.length == 0) {
            throw new ResourceLoadException("Tilemap decode produced no bytes");
        }
        return parse(bytes);
    }

    public static TileMapAsset parse(byte[] jsonBytes) {
        JsonValue root = new JsonReader().parse(new String(jsonBytes, java.nio.charset.StandardCharsets.UTF_8));
        return parse(root);
    }

    public static TileMapAsset parse(JsonValue root) {
        if (root == null || !root.isObject()) {
            throw new ResourceLoadException("Tilemap root must be an object");
        }
        int version = root.getInt("version", -1);
        if (version != 1) {
            throw new ResourceLoadException("Tilemap version must be 1");
        }
        int tileWidth = root.getInt("tileWidth", 0);
        int tileHeight = root.getInt("tileHeight", 0);
        int width = root.getInt("width", 0);
        int height = root.getInt("height", 0);
        String tileset = root.getString("tileset", "").trim();
        if (tileset.isEmpty()) {
            throw new ResourceLoadException("Tilemap tileset is required");
        }
        JsonValue layersValue = root.get("layers");
        if (layersValue == null || !layersValue.isArray()) {
            throw new ResourceLoadException("Tilemap layers must be an array");
        }
        Map<String, int[]> layers = new LinkedHashMap<>();
        for (JsonValue layerValue : layersValue) {
            String name = layerValue.getString("name", "").trim();
            if (name.isEmpty()) {
                throw new ResourceLoadException("Tilemap layer name is required");
            }
            JsonValue tilesValue = layerValue.get("tiles");
            if (tilesValue == null || !tilesValue.isArray()) {
                throw new ResourceLoadException("Tilemap layer '" + name + "' tiles must be an array");
            }
            int expected = width * height;
            int[] tiles = new int[expected];
            int index = 0;
            for (JsonValue tile : tilesValue) {
                if (index >= expected) {
                    break;
                }
                tiles[index++] = tile.asInt();
            }
            if (index != expected) {
                throw new ResourceLoadException(
                        "Tilemap layer '" + name + "' expected " + expected + " tiles, got " + index);
            }
            layers.put(name, tiles);
        }
        if (layers.isEmpty()) {
            throw new ResourceLoadException("Tilemap must have at least one layer");
        }
        return new TileMapAsset(tileWidth, tileHeight, width, height, tileset, layers);
    }

    @Override
    public void dispose(Object resource) {
        // CPU-only asset; nothing to dispose.
    }
}
