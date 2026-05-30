package dev.hermes.core.ecs;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Transform;

import java.util.Comparator;
import java.util.List;

/**
 * Sorts drawable entities for the active camera projection.
 */
public final class SpriteDrawOrder {

    private SpriteDrawOrder() {
    }

    public static void sort(List<Entity> drawables, EntityStore store, ActiveCamera camera) {
        if (camera.projection() == Camera.Projection.PERSPECTIVE) {
            sortByDistanceFromCamera(drawables, store, camera);
        } else {
            drawables.sort(Comparator.comparingDouble(e -> store.getComponent(e.id(), Transform.class).z()));
        }
    }

    private static void sortByDistanceFromCamera(
            List<Entity> drawables, EntityStore store, ActiveCamera camera) {
        float cx = camera.x();
        float cy = camera.y();
        float cz = camera.z();
        drawables.sort(
                Comparator.<Entity>comparingDouble(
                                e -> {
                                    Transform t = store.getComponent(e.id(), Transform.class);
                                    float dx = t.x() - cx;
                                    float dy = t.y() - cy;
                                    float dz = t.z() - cz;
                                    return dx * dx + dy * dy + dz * dz;
                                })
                        .reversed());
    }
}
