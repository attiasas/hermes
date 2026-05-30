package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.input.PickLayer;

/**
 * Marks an entity as pickable in screen-space. Pair with {@link Transform} for center position.
 */
public final class Selectable implements Component {

    private boolean enabled = true;
    private float radius = 16f;
    private PickLayer layer = PickLayer.WORLD;

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float radius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public PickLayer layer() {
        return layer;
    }

    public void setLayer(PickLayer layer) {
        this.layer = layer == null ? PickLayer.WORLD : layer;
    }
}
