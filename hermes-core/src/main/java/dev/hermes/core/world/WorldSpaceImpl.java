package dev.hermes.core.world;

import dev.hermes.api.world.WorldSpace;
import dev.hermes.api.world.WorldKind;
import dev.hermes.api.world.WorldBounds;
import dev.hermes.api.world.SpatialIndex;
import dev.hermes.api.Entity;
import java.util.List;
import java.util.Optional;
import dev.hermes.core.world.spatial.BruteForceSpatialIndex;
import dev.hermes.core.world.tilemap.TileMapAsset;

import java.util.Optional;

public class WorldSpaceImpl implements WorldSpace {

    private WorldKind kind;
    private WorldBounds bounds;
    private SpatialIndex spatial;
    private Optional<String> tilemapPath = Optional.empty();
    private Optional<TileMapAsset> tileMapAsset = Optional.empty();

    public WorldSpaceImpl() {
        this(WorldKind.OPEN, WorldBounds.unbounded(), new BruteForceSpatialIndex());
    }

    public WorldSpaceImpl(WorldKind kind, WorldBounds bounds, SpatialIndex spatial) {
        this.kind = kind;
        this.bounds = bounds;
        this.spatial = spatial;
    }

    public void configure(WorldBlock block, SpatialIndex spatialIndex) {
        configure(block, spatialIndex, Optional.empty());
    }

    public void configure(WorldBlock block, SpatialIndex spatialIndex, Optional<TileMapAsset> tileMap) {
        this.kind = block.kind();
        this.bounds = block.bounds();
        this.spatial = spatialIndex;
        this.tilemapPath = block.tilemapPath();
        this.tileMapAsset = tileMap == null ? Optional.empty() : tileMap;
    }

    public Optional<TileMapAsset> tileMapAsset() {
        return tileMapAsset;
    }

    public Optional<String> tilemapPath() {
        return tilemapPath;
    }

    @Override
    public WorldKind kind() {
        return kind;
    }

    @Override
    public WorldBounds bounds() {
        return bounds;
    }

    @Override
    public SpatialIndex spatial() {
        return spatial;
    }

    @Override
    public List<Entity> queryNear(float x, float y, float radius) {
        return spatial().queryNear(x, y, radius);
    }

    @Override
    public List<Entity> queryNear(float x, float y, float z, float radius) {
        return spatial().queryNear(x, y, z, radius);
    }

    @Override
    public List<Entity> queryAabb(float minX, float minY, float maxX, float maxY) {
        return spatial().queryAabb(minX, minY, maxX, maxY);
    }
}
