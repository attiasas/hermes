package dev.hermes.core.world;

import dev.hermes.api.world.SpatialIndexFactory;
import dev.hermes.api.world.SpatialIndexRegistrar;
import dev.hermes.core.world.spatial.BruteForceSpatialIndex;
import dev.hermes.core.world.spatial.TileMapSpatialIndex;
import dev.hermes.core.world.spatial.UniformGridSpatialIndex;
import dev.hermes.core.world.tilemap.TileMapAsset;

import dev.hermes.api.world.SpatialIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Built-in and SPI-registered spatial index strategies. */
public final class SpatialIndexStrategyRegistry implements SpatialIndexRegistrar {

    private final Map<String, SpatialIndexFactory> factories = new HashMap<>();

    @Override
    public void register(String strategyId, SpatialIndexFactory factory) {
        if (strategyId == null || strategyId.isBlank()) {
            throw new IllegalArgumentException("strategyId is required");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory is required");
        }
        factories.put(strategyId.trim(), factory);
    }

    SpatialIndex create(WorldBlock block, Optional<TileMapAsset> tilemap) {
        SpatialIndexFactory custom = factories.get(block.spatialStrategy());
        if (custom != null) {
            return custom.create(block.spatialCellSize());
        }
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
