package dev.hermes.core.world;

import dev.hermes.api.world.WorldBounds;
import dev.hermes.api.world.WorldKind;
import dev.hermes.api.world.WorldSpace;
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
        SpatialIndexStrategyRegistry strategies = SpatialIndexRegistrations.registry();
        ((WorldSpaceImpl) space).configure(resolved, strategies.create(resolved, tilemap), tilemap);
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
}
