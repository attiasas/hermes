package dev.hermes.core.world;

import dev.hermes.api.world.WorldSpace;
import dev.hermes.core.world.spatial.BruteForceSpatialIndex;

/** Applies a parsed {@link WorldBlock} to a scene {@link WorldSpace}. */
public final class WorldBlockApplier {

    private WorldBlockApplier() {}

    public static void apply(WorldSpace space, WorldBlock block) {
        if (!(space instanceof WorldSpaceImpl)) {
            throw new IllegalArgumentException("WorldSpace implementation not supported: " + space.getClass());
        }
        ((WorldSpaceImpl) space).configure(block, createSpatialIndex(block));
    }

    private static dev.hermes.api.world.SpatialIndex createSpatialIndex(WorldBlock block) {
        String strategy = block.spatialStrategy();
        if ("uniformGrid".equals(strategy) || "tilemap".equals(strategy)) {
            // Task 7/8 register specialized indexes; fall back until then.
            return new BruteForceSpatialIndex();
        }
        return new BruteForceSpatialIndex();
    }
}
