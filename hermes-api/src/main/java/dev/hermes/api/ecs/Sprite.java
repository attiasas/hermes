package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/**
 * Drawable sprite referencing a texture path under the assets root.
 */
public final class Sprite implements Component {

    private String texture;

    public Sprite() {
    }

    public Sprite(String texture) {
        this.texture = texture;
    }

    public String texture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }
}
