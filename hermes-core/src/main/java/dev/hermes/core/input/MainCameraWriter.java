package dev.hermes.core.input;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.world.CameraControlsConfig;
import dev.hermes.api.world.SceneCameraConfig;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import java.util.Optional;

/** Writes resolved {@link ActiveCamera} state to scene config or bound entity camera. */
public final class MainCameraWriter {

    private MainCameraWriter() {}

    public static void write(
            WorldManager manager,
            ActiveCamera active,
            float targetX,
            float targetY,
            float targetZ,
            CameraControlsConfig cfg) {
        Optional<Entity> entity = CameraResolver.mainCameraEntity(manager);
        if (entity.isPresent()) {
            writeEntity(manager, entity.get(), active, targetX, targetY, targetZ, cfg);
        } else {
            writeScene(manager, active, targetX, targetY, targetZ);
        }
    }

    private static void writeScene(
            WorldManager manager, ActiveCamera active, float targetX, float targetY, float targetZ) {
        SceneCameraConfig config = manager.camera().sceneConfig();
        config.setX(active.x());
        config.setY(active.y());
        config.setZ(active.z());
        config.setRotationX(active.rotationX());
        config.setRotationY(active.rotationY());
        config.setRotationZ(active.rotationZ());
        config.setLookAt(targetX, targetY, targetZ);
    }

    private static void writeEntity(
            WorldManager manager,
            Entity entity,
            ActiveCamera active,
            float targetX,
            float targetY,
            float targetZ,
            CameraControlsConfig cfg) {
        Transform transform = manager.entities().getComponent(entity.id(), Transform.class);
        Camera camera = manager.entities().getComponent(entity.id(), Camera.class);
        if (transform == null || camera == null) {
            return;
        }
        transform.setX(active.x());
        transform.setY(active.y());
        transform.setZ(active.z());
        transform.setRotationX(active.rotationX());
        transform.setRotationY(active.rotationY());
        transform.setRotationZ(active.rotationZ());
        camera.setLookAt(targetX, targetY, targetZ);
    }
}
