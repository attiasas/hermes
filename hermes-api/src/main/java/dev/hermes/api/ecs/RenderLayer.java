package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/**
 * Selects world-space or UI overlay rendering for an entity.
 */
public final class RenderLayer implements Component {

    public enum Layer {
        WORLD,
        UI
    }

    private Layer layer = Layer.WORLD;

    public Layer layer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer == null ? Layer.WORLD : layer;
    }
}
