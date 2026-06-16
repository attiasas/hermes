package dev.hermes.core.world.spatial;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.world.SpatialIndex;
import dev.hermes.core.world.tilemap.TileMapAsset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Spatial index with one cell per map tile. */
public final class TileMapSpatialIndex implements SpatialIndex {

    private final float tileWidth;
    private final float tileHeight;
    private final Map<Long, List<Entity>> cells = new HashMap<>();
    private EntityStore entities;

    public TileMapSpatialIndex(TileMapAsset asset) {
        this.tileWidth = asset.tileWidth();
        this.tileHeight = asset.tileHeight();
    }

    public TileMapSpatialIndex(float tileWidth, float tileHeight) {
        if (tileWidth <= 0f || tileHeight <= 0f) {
            throw new IllegalArgumentException("tile dimensions must be positive");
        }
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
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
            long key = cellKey(tileX(transform.x()), tileY(transform.y()));
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
        int cx = tileX(x);
        int cy = tileY(y);
        int tileRadiusX = Math.max(1, (int) Math.ceil(radius / tileWidth));
        int tileRadiusY = Math.max(1, (int) Math.ceil(radius / tileHeight));
        for (int dx = -tileRadiusX; dx <= tileRadiusX; dx++) {
            for (int dy = -tileRadiusY; dy <= tileRadiusY; dy++) {
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

    private int tileX(float x) {
        return (int) Math.floor(x / tileWidth);
    }

    private int tileY(float y) {
        return (int) Math.floor(y / tileHeight);
    }

    private static long cellKey(int tx, int ty) {
        return (((long) tx) << 32) ^ (ty & 0xffffffffL);
    }
}
