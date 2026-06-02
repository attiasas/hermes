package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/**
 * Pick/query radius; 0 = point. Default inferred from Selectable when absent.
 */
public final class SpatialPresence implements Component {

    private float radius = 0f;
    private float halfWidth = 0f;
    private float halfHeight = 0f;

    public float radius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float halfWidth() {
        return halfWidth;
    }

    public void setHalfWidth(float halfWidth) {
        this.halfWidth = halfWidth;
    }

    public float halfHeight() {
        return halfHeight;
    }

    public void setHalfHeight(float halfHeight) {
        this.halfHeight = halfHeight;
    }
}
