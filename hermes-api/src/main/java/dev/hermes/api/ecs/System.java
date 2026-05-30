package dev.hermes.api.ecs;

/**
 * Ordered engine system invoked each frame.
 */
public interface System {

    default void update(WorldManager manager, float deltaSeconds) {
    }

    default void render(WorldManager manager) {
    }
}
