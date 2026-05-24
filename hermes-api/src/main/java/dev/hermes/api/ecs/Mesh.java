package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/**
 * 3D mesh referencing a model path and optional texture under the assets root.
 */
public final class Mesh implements Component {

    private String model;
    private String texture;

    public Mesh() {
    }

    public Mesh(String model) {
        this.model = model;
    }

    public String model() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String texture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }
}
