package dev.hermes.core.world.spatial;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.world.SpatialIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UniformGridSpatialIndex implements SpatialIndex {

    private final float cellSize;
    private final Map<Long, List<Entity>> cells = new HashMap<>();
    private EntityStore entities;

    public UniformGridSpatialIndex(float cellSize) {
        this.cellSize = cellSize > 0f ? cellSize : 128f;
    }

    @Override
    public void rebuild(EntityStore entities) {
        this.entities = entities;
        cells.clear();
        for (Entity entity : entities.entities()) {
            if (!entities.hasComponent(entity.id(), Transform.class)) {
                continue;
            }
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            long key = cellKey(cellX(transform.x()), cellY(transform.y()));
            cells.computeIfAbsent(key, k -> new ArrayList<>()).add(entity);
        }
    }

    @Override
    public List<Entity> queryNear(float x, float y, float radius) {
        return queryNear(x, y, 0f, radius);
    }

    @Override
    public List<Entity> queryNear(float x, float y, float z, float radius) {
        Set<Entity> seen = new HashSet<>();
        List<Entity> out = new ArrayList<>();
        int cx = cellX(x);
        int cy = cellY(y);
        int cellRadius = Math.max(1, (int) Math.ceil(radius / cellSize));
        for (int dx = -cellRadius; dx <= cellRadius; dx++) {
            for (int dy = -cellRadius; dy <= cellRadius; dy++) {
                List<Entity> bucket = cells.get(cellKey(cx + dx, cy + dy));
                if (bucket == null) {
                    continue;
                }
                for (Entity entity : bucket) {
                    if (!seen.add(entity)) {
                        continue;
                    }
                    Transform transform = entities.getComponent(entity.id(), Transform.class);
                    if (transform != null && SpatialBoundsHelper.withinRadius3D(transform, x, y, z, radius)) {
                        out.add(entity);
                    }
                }
            }
        }
        return out;
    }

    @Override
    public List<Entity> queryAabb(float minX, float minY, float maxX, float maxY) {
        BruteForceSpatialIndex fallback = new BruteForceSpatialIndex();
        fallback.rebuild(entities);
        return fallback.queryAabb(minX, minY, maxX, maxY);
    }

    private int cellX(float x) {
        return (int) Math.floor(x / cellSize);
    }

    private int cellY(float y) {
        return (int) Math.floor(y / cellSize);
    }

    private static long cellKey(int cx, int cy) {
        return (((long) cx) << 32) ^ (cy & 0xffffffffL);
    }
}
