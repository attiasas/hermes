package dev.hermes.api.world;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.Entity;
import java.util.List;

public interface SpatialIndex {

    void rebuild(EntityStore entities);

    List<Entity> queryNear(float x, float y, float radius);

    List<Entity> queryNear(float x, float y, float z, float radius);

    List<Entity> queryAabb(float minX, float minY, float maxX, float maxY);
}
