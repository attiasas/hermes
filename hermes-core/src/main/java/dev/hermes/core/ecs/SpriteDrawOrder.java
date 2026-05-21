package dev.hermes.core.ecs;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import java.util.Comparator;
import java.util.List;

/** Sorts drawable entities for the active camera projection. */
public final class SpriteDrawOrder {

  private SpriteDrawOrder() {}

  public static void sort(List<Entity> entities, World world, ActiveCamera camera) {
    if (camera.projection() == Camera.Projection.PERSPECTIVE) {
      sortByDistanceFromCamera(entities, world, camera);
    } else {
      entities.sort(Comparator.comparingDouble(e -> world.getComponent(e.id(), Transform.class).z()));
    }
  }

  private static void sortByDistanceFromCamera(
      List<Entity> entities, World world, ActiveCamera camera) {
    float cx = camera.x();
    float cy = camera.y();
    float cz = camera.z();
    entities.sort(
        Comparator.<Entity>comparingDouble(
                e -> {
                  Transform t = world.getComponent(e.id(), Transform.class);
                  float dx = t.x() - cx;
                  float dy = t.y() - cy;
                  float dz = t.z() - cz;
                  return dx * dx + dy * dy + dz * dz;
                })
            .reversed());
  }
}
