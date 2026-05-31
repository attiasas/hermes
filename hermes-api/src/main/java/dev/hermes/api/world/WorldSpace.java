package dev.hermes.api.world;

import java.util.List;
import java.util.Optional;

import dev.hermes.api.Entity;
import dev.hermes.api.world.SpatialIndex;
import dev.hermes.api.world.WorldBounds;
import dev.hermes.api.world.WorldKind;

public interface WorldSpace {
    WorldKind kind();
    WorldBounds bounds();
    SpatialIndex spatial();

    /** Entities within radius of (x,y) in world units. */
    List<Entity> queryNear(float x, float y, float radius);

    /** Entities within sphere. */
    List<Entity> queryNear(float x, float y, float z, float radius);

    /** Entities whose SpatialPresence intersects AABB. */
    List<Entity> queryAabb(float minX, float minY, float maxX, float maxY);
}
