package dev.hermes.api.world;

/** Creates a {@link SpatialIndex} for a registered strategy id. */
@FunctionalInterface
public interface SpatialIndexFactory {

  SpatialIndex create(float cellSize);
}
