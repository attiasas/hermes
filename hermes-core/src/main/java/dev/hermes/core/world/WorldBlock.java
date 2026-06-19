package dev.hermes.core.world;

import dev.hermes.api.world.WorldBounds;
import dev.hermes.api.world.WorldKind;

import java.util.Optional;

/** Parsed scene JSON {@code "world"} block (version 1). */
public final class WorldBlock {

    private final WorldKind kind;
    private final WorldBounds bounds;
    private final String spatialStrategy;
    private final float spatialCellSize;
    private final Optional<String> tilemapPath;

    public WorldBlock(
            WorldKind kind,
            WorldBounds bounds,
            String spatialStrategy,
            float spatialCellSize,
            Optional<String> tilemapPath) {
        this.kind = kind == null ? WorldKind.OPEN : kind;
        this.bounds = bounds == null ? WorldBounds.unbounded() : bounds;
        this.spatialStrategy = spatialStrategy == null || spatialStrategy.isBlank() ? "bruteForce" : spatialStrategy;
        this.spatialCellSize = spatialCellSize > 0f ? spatialCellSize : 128f;
        this.tilemapPath = tilemapPath == null ? Optional.empty() : tilemapPath;
    }

    public WorldKind kind() {
        return kind;
    }

    public WorldBounds bounds() {
        return bounds;
    }

    public String spatialStrategy() {
        return spatialStrategy;
    }

    public float spatialCellSize() {
        return spatialCellSize;
    }

    public Optional<String> tilemapPath() {
        return tilemapPath;
    }
}
