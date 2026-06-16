package dev.hermes.core.input;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.world.SceneCameraConfig;

/** Orbit / pan reference point for built-in camera controls. */
public final class CameraControlTarget {

    private final float x;
    private final float y;
    private final float z;
    private final boolean fromSelection;

    private CameraControlTarget(float x, float y, float z, boolean fromSelection) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fromSelection = fromSelection;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public boolean fromSelection() {
        return fromSelection;
    }

    public static CameraControlTarget resolve(WorldManager manager) {
        for (Entity entity : manager.entities().entitiesWith(Selected.class)) {
            Transform transform = manager.entities().getComponent(entity.id(), Transform.class);
            if (transform != null) {
                return new CameraControlTarget(transform.x(), transform.y(), transform.z(), true);
            }
        }
        SceneCameraConfig config = manager.camera().sceneConfig();
        if (!Float.isNaN(config.lookAtX())) {
            return new CameraControlTarget(config.lookAtX(), config.lookAtY(), config.lookAtZ(), false);
        }
        return new CameraControlTarget(0f, 0f, 0f, false);
    }
}
