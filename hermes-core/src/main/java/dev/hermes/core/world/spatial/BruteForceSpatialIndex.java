package dev.hermes.core.world.spatial;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.Entity;
import dev.hermes.api.world.SpatialIndex;
import java.util.List;

public final class BruteForceSpatialIndex implements SpatialIndex {

    @Override
    public void rebuild(EntityStore entities) {
        // TODO: Implement
    }

    @Override
    public List<Entity> queryNear(float x, float y, float radius) {
        return List.of();
    }

    @Override
    public List<Entity> queryNear(float x, float y, float z, float radius) {
        return List.of();
    }

    @Override
    public List<Entity> queryAabb(float minX, float minY, float maxX, float maxY) {
        return List.of();
    }
}
