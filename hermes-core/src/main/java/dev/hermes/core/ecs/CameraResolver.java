package dev.hermes.core.ecs;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;

/**
 * Selects the active scene camera from ECS entities.
 */
public final class CameraResolver {

    private CameraResolver() {
    }

    public static ActiveCamera resolve(World world, float windowWidth, float windowHeight) {
        Entity activeEntity = null;
        Camera activeCamera = null;
        Entity fallbackEntity = null;
        Camera fallbackCamera = null;
        int activeCount = 0;

        for (Entity entity : world.entitiesWith(Camera.class)) {
            Camera camera = world.getComponent(entity.id(), Camera.class);
            if (camera == null) {
                continue;
            }
            if (fallbackEntity == null) {
                fallbackEntity = entity;
                fallbackCamera = camera;
            }
            if (camera.active()) {
                activeCount++;
                if (activeEntity == null) {
                    activeEntity = entity;
                    activeCamera = camera;
                }
            }
        }

        if (activeCount > 1) {
            System.err.println(
                    "Warning: multiple active Camera components found; using the first active camera.");
        }

        Entity chosen = activeEntity != null ? activeEntity : fallbackEntity;
        Camera chosenCamera = activeCamera != null ? activeCamera : fallbackCamera;

        if (chosen == null || chosenCamera == null) {
            return defaultCamera(windowWidth, windowHeight);
        }

        Transform transform = world.getComponent(chosen.id(), Transform.class);
        if (transform == null) {
            throw new IllegalStateException(
                    "Camera entity '"
                            + chosen.name()
                            + "' must have a Transform component on the same entity.");
        }

        float viewportWidth =
                chosenCamera.viewportWidth() > 0f ? chosenCamera.viewportWidth() : windowWidth;
        float viewportHeight =
                chosenCamera.viewportHeight() > 0f ? chosenCamera.viewportHeight() : windowHeight;

        return fromComponents(transform, chosenCamera, viewportWidth, viewportHeight);
    }

    /**
     * Resolves the camera on a named entity; falls back to {@link #resolve} when not found.
     */
    public static ActiveCamera resolveNamed(
            World world, String entityName, float windowWidth, float windowHeight) {
        if (entityName == null || entityName.isBlank()) {
            return resolve(world, windowWidth, windowHeight);
        }
        Entity entity = world.findByName(entityName);
        if (entity == null) {
            System.err.println(
                    "Warning: UI pass camera entity '" + entityName + "' not found; using active camera.");
            return resolve(world, windowWidth, windowHeight);
        }
        Camera camera = world.getComponent(entity.id(), Camera.class);
        Transform transform = world.getComponent(entity.id(), Transform.class);
        if (camera == null || transform == null) {
            throw new IllegalStateException(
                    "Camera entity '"
                            + entityName
                            + "' must have Camera and Transform components on the same entity.");
        }
        float viewportWidth =
                camera.viewportWidth() > 0f ? camera.viewportWidth() : windowWidth;
        float viewportHeight =
                camera.viewportHeight() > 0f ? camera.viewportHeight() : windowHeight;
        return fromComponents(transform, camera, viewportWidth, viewportHeight);
    }

    private static ActiveCamera fromComponents(
            Transform transform, Camera camera, float viewportWidth, float viewportHeight) {
        return new ActiveCamera(
                camera.projection(),
                transform.x(),
                transform.y(),
                transform.z(),
                transform.rotationX(),
                transform.rotationY(),
                transform.rotationZ(),
                camera.zoom(),
                camera.fieldOfView(),
                camera.near(),
                camera.far(),
                viewportWidth,
                viewportHeight);
    }

    private static ActiveCamera defaultCamera(float windowWidth, float windowHeight) {
        return new ActiveCamera(
                Camera.Projection.ORTHOGRAPHIC,
                windowWidth * 0.5f,
                windowHeight * 0.5f,
                0f,
                0f,
                0f,
                0f,
                1f,
                67f,
                0.1f,
                3000f,
                windowWidth,
                windowHeight);
    }
}
