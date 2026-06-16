package dev.hermes.core.world;

import dev.hermes.api.world.WorldBounds;
import dev.hermes.api.world.WorldKind;
import dev.hermes.api.world.WorldSpace;
import dev.hermes.core.world.spatial.BruteForceSpatialIndex;
import dev.hermes.core.world.spatial.TileMapSpatialIndex;
import dev.hermes.core.world.spatial.UniformGridSpatialIndex;
import dev.hermes.core.world.tilemap.HermesTileMapLoader;
import dev.hermes.core.world.tilemap.TileMapAsset;

import java.util.Optional;

/** Applies a parsed {@link WorldBlock} to a scene {@link WorldSpace}. */
public final class WorldBlockApplier {

    private WorldBlockApplier() {}

    public static void apply(WorldSpace space, WorldBlock block) {
        if (!(space instanceof WorldSpaceImpl)) {
            throw new IllegalArgumentException("WorldSpace implementation not supported: " + space.getClass());
        }
        Optional<TileMapAsset> tilemap = Optional.empty();
        WorldBounds bounds = block.bounds();
        if (block.kind() == WorldKind.TILEMAP && block.tilemapPath().isPresent()) {
            tilemap = Optional.of(loadTilemap(block.tilemapPath().get()));
            bounds = boundsFromTilemap(tilemap.get());
        }
        WorldBlock resolved =
                new WorldBlock(block.kind(), bounds, block.spatialStrategy(), block.spatialCellSize(), block.tilemapPath());
        ((WorldSpaceImpl) space).configure(resolved, createSpatialIndex(resolved, tilemap), tilemap);
    }

    private static TileMapAsset loadTilemap(String path) {
        HermesTileMapLoader loader = new HermesTileMapLoader();
        return (TileMapAsset) loader.upload(loader.decode(path));
    }

    private static WorldBounds boundsFromTilemap(TileMapAsset asset) {
        return WorldBounds.bounded(
                0f,
                0f,
                Float.NEGATIVE_INFINITY,
                asset.worldWidth(),
                asset.worldHeight(),
                Float.POSITIVE_INFINITY);
    }

    private static dev.hermes.api.world.SpatialIndex createSpatialIndex(
            WorldBlock block, Optional<TileMapAsset> tilemap) {
        if ("tilemap".equals(block.spatialStrategy())) {
            if (tilemap.isPresent()) {
                return new TileMapSpatialIndex(tilemap.get());
            }
            return new BruteForceSpatialIndex();
        }
        if ("uniformGrid".equals(block.spatialStrategy())) {
            return new UniformGridSpatialIndex(block.spatialCellSize());
        }
        return new BruteForceSpatialIndex();
    }
}
