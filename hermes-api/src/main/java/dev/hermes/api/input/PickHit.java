package dev.hermes.api.input;

import dev.hermes.api.EntityId;

/**
 * Result of a screen-space pick against selectable entities.
 */
public final class PickHit {

    public final EntityId entity;
    public final String entityName;
    public final float worldX;
    public final float worldY;
    public final float worldZ;
    public final float distance;

    public PickHit(
            EntityId entity,
            String entityName,
            float worldX,
            float worldY,
            float worldZ,
            float distance) {
        this.entity = entity;
        this.entityName = entityName;
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
        this.distance = distance;
    }
}
