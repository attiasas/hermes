package dev.hermes.api.ecs;

import dev.hermes.api.Component;

import java.util.List;

/**
 * Multi-part drawable component for mesh and sprite parts on an entity.
 */
public final class Drawables implements Component {

    private final List<DrawablePart> parts;

    public Drawables(List<DrawablePart> parts) {
        this.parts = List.copyOf(parts);
    }

    public static Drawables singleMesh(String model) {
        return new Drawables(List.of(DrawablePart.mesh("default", model)));
    }

    public static Drawables singleSprite(String texture) {
        return new Drawables(List.of(DrawablePart.sprite("default", texture)));
    }

    public List<DrawablePart> parts() {
        return parts;
    }
}
