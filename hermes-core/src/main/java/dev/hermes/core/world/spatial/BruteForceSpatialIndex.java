package dev.hermes.core.world.spatial;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.SpatialPresence;
import dev.hermes.api.Entity;
import dev.hermes.api.world.SpatialIndex;
import java.util.List;
import java.util.stream.Collectors;
import dev.hermes.api.ecs.Transform;
import java.util.ArrayList;

public final class BruteForceSpatialIndex implements SpatialIndex {

    private List<Entity> indexed;
    private EntityStore entities;

    @Override
    public void rebuild(EntityStore entities) {
        this.entities = entities;
        this.indexed = entities.entities().stream()
            .filter(e -> entities.hasComponent(e.id(), Transform.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<Entity> queryNear(float x, float y, float radius) {
        List<Entity> out = new ArrayList<>();

        for (Entity entity : indexed) {
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (transform != null && SpatialBoundsHelper.withinRadius2D(transform, x, y, radius)) {
                out.add(entity);
            }
        }

        return out;
    }

    @Override
    public List<Entity> queryNear(float x, float y, float z, float radius) {
        List<Entity> out = new ArrayList<>();
        
        for (Entity entity : indexed) {
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (transform != null && SpatialBoundsHelper.withinRadius3D(transform, x, y, z, radius)) {
                out.add(entity);
            }
        }
    
        return out;
    }

    @Override
    public List<Entity> queryAabb(float minX, float minY, float maxX, float maxY) {
        List<Entity> out = new ArrayList<>();
        
        for (Entity entity : indexed) {
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            SpatialPresence presence = entities.getComponent(entity.id(), SpatialPresence.class);
    
            if (transform == null || presence == null) {
                continue;
            }
    
            if (SpatialBoundsHelper.intersectsAabb(transform, presence, minX, minY, maxX, maxY)) {
                out.add(entity);
            }
        }

        return out;
    }
}
