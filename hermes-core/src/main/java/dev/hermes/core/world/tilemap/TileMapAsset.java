package dev.hermes.core.world.tilemap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Loaded Hermes tilemap asset (`.hmap.json` version 1). */
public final class TileMapAsset {

    private final int tileWidth;
    private final int tileHeight;
    private final int width;
    private final int height;
    private final String tileset;
    private final Map<String, int[]> layers;

    public TileMapAsset(
            int tileWidth,
            int tileHeight,
            int width,
            int height,
            String tileset,
            Map<String, int[]> layers) {
        if (tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException("tile dimensions must be positive");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("map dimensions must be positive");
        }
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.width = width;
        this.height = height;
        this.tileset = Objects.requireNonNull(tileset, "tileset");
        Map<String, int[]> copy = new LinkedHashMap<>();
        for (Map.Entry<String, int[]> entry : layers.entrySet()) {
            int[] tiles = entry.getValue();
            if (tiles == null || tiles.length != width * height) {
                throw new IllegalArgumentException(
                        "layer '" + entry.getKey() + "' must have " + (width * height) + " tiles");
            }
            copy.put(entry.getKey(), tiles.clone());
        }
        this.layers = Collections.unmodifiableMap(copy);
    }

    public int tileWidth() {
        return tileWidth;
    }

    public int tileHeight() {
        return tileHeight;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public float worldWidth() {
        return width * (float) tileWidth;
    }

    public float worldHeight() {
        return height * (float) tileHeight;
    }

    public String tileset() {
        return tileset;
    }

    public Map<String, int[]> layers() {
        return layers;
    }

    public Optional<int[]> layer(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        int[] tiles = layers.get(name);
        return tiles == null ? Optional.empty() : Optional.of(tiles);
    }
}
