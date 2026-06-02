package dev.hermes.core.world.spatial;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.SpatialPresence;
import dev.hermes.api.ecs.Transform;

public final class SpatialBoundsHelper {

    private SpatialBoundsHelper() {}

    
    public static float effectiveRadius(EntityStore entities, Entity e) {
        SpatialPresence sp = entities.getComponent(e.id(), SpatialPresence.class);
        if (sp == null) return 0f;
        return sp.radius() + sp.halfWidth() + sp.halfHeight();
    }

    public static boolean withinRadius2D(
            Transform transform,
            float x,
            float y,
            float radius
    ) {
        float dx = transform.x() - x;
        float dy = transform.y() - y;

        return dx * dx + dy * dy <= radius * radius;
    }

    public static boolean withinRadius3D(
            Transform transform,
            float x,
            float y,
            float z,
            float radius
    ) {
        float dx = transform.x() - x;
        float dy = transform.y() - y;
        float dz = transform.z() - z;

        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    public static boolean intersectsAabb(
            Transform transform,
            SpatialPresence presence,
            float minX,
            float minY,
            float maxX,
            float maxY
    ) {
        float entityMinX = transform.x() - presence.halfWidth();
        float entityMaxX = transform.x() + presence.halfWidth();

        float entityMinY = transform.y() - presence.halfHeight();
        float entityMaxY = transform.y() + presence.halfHeight();

        // If width/height are not configured, fall back to radius.
        if (presence.halfWidth() == 0f && presence.halfHeight() == 0f) {
            entityMinX = transform.x() - presence.radius();
            entityMaxX = transform.x() + presence.radius();

            entityMinY = transform.y() - presence.radius();
            entityMaxY = transform.y() + presence.radius();
        }

        return entityMaxX >= minX
                && entityMinX <= maxX
                && entityMaxY >= minY
                && entityMinY <= maxY;
    }
}
