package dev.hermes.api.ecs;

/** Ordered engine system invoked each frame. */
public interface System {

  default void update(World world, float deltaSeconds) {}

  default void render(World world) {}
}
