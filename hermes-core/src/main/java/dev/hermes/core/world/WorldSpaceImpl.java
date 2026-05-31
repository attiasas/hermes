package dev.hermes.core.world;

import dev.hermes.api.world.WorldSpace;
import dev.hermes.api.world.WorldKind;
import dev.hermes.api.world.WorldBounds;
import dev.hermes.api.world.SpatialIndex;
import dev.hermes.api.Entity;
import java.util.List;
import dev.hermes.core.world.spatial.BruteForceSpatialIndex;

public class WorldSpaceImpl implements WorldSpace {

    private final WorldKind kind;
    private final WorldBounds bounds;
    private final SpatialIndex spatial;


    public WorldSpaceImpl() {
        this(WorldKind.OPEN, WorldBounds.unbounded(), new BruteForceSpatialIndex());
    }

    public WorldSpaceImpl(WorldKind kind, WorldBounds bounds, SpatialIndex spatial) {
        this.kind = kind;
        this.bounds = bounds;
        this.spatial = spatial;
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
