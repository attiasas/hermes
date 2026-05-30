package dev.hermes.core.ecs;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;

import java.util.Optional;

/**
 * Selects the active scene camera from ECS entities.
 */
public final class CameraResolver {

    private CameraResolver() {
    }

    /**
     * Resolves the camera for a render pass using surface pixel dimensions (not window size).
     * When {@code passTargetId} is not {@code "screen"}, prefers a camera whose
     * {@link Camera#renderTarget()} matches the pass target.
     */
    public static ActiveCamera resolveForPass(
            EntityStore entities, String passTargetId, float surfaceWidth, float surfaceHeight) {
        if (entities == null) {
            return defaultCamera(surfaceWidth, surfaceHeight);
        }
        Entity renderTargetEntity = null;
        Camera renderTargetCamera = null;
        Entity activeEntity = null;
        Camera activeCamera = null;
        Entity fallbackEntity = null;
        Camera fallbackCamera = null;
        int activeCount = 0;
        boolean matchTarget = passTargetId != null && !passTargetId.isBlank() && !"screen".equals(passTargetId);

        for (Entity entity : entities.entitiesWith(Camera.class)) {
            Camera camera = entities.getComponent(entity.id(), Camera.class);
            if (camera == null) {
                continue;
            }
            if (fallbackEntity == null) {
                fallbackEntity = entity;
                fallbackCamera = camera;
            }
            if (matchTarget && passTargetId.equals(camera.renderTarget())) {
                if (renderTargetEntity == null) {
                    renderTargetEntity = entity;
                    renderTargetCamera = camera;
                }
            }
            if (camera.active()) {
                activeCount++;
                if (activeEntity == null) {
                    activeEntity = entity;
                    activeCamera = camera;
                }
            }
        }

        if (activeCount > 1 && renderTargetEntity == null) {
            System.err.println(
                    "Warning: multiple active Camera components found; using the first active camera.");
        }

        Entity chosen =
                renderTargetEntity != null
                        ? renderTargetEntity
                        : (activeEntity != null ? activeEntity : fallbackEntity);
        Camera chosenCamera =
                renderTargetCamera != null
                        ? renderTargetCamera
                        : (activeCamera != null ? activeCamera : fallbackCamera);

        if (chosen == null || chosenCamera == null) {
            return defaultCamera(surfaceWidth, surfaceHeight);
        }

        Transform transform = entities.getComponent(chosen.id(), Transform.class);
        if (transform == null) {
            throw new IllegalStateException(
                    "Camera entity '"
                            + chosen.name()
                            + "' must have a Transform component on the same entity.");
        }

        float viewportWidth =
                chosenCamera.viewportWidth() > 0f ? chosenCamera.viewportWidth() : surfaceWidth;
        float viewportHeight =
                chosenCamera.viewportHeight() > 0f ? chosenCamera.viewportHeight() : surfaceHeight;

        return fromComponents(transform, chosenCamera, viewportWidth, viewportHeight);
    }

    public static ActiveCamera resolve(EntityStore entities, float windowWidth, float windowHeight) {
        return resolveForPass(entities, "screen", windowWidth, windowHeight);
    }

    /** First active camera entity with Transform, same choice as {@link #resolveForPass}. */
    public static Optional<Entity> activeCameraEntity(EntityStore entities) {
        if (entities == null) {
            return Optional.empty();
        }
        Entity active = null;
        Entity fallback = null;
        for (Entity entity : entities.entitiesWith(Camera.class)) {
            Camera camera = entities.getComponent(entity.id(), Camera.class);
            if (camera == null || entities.getComponent(entity.id(), Transform.class) == null) {
                continue;
            }
            if (fallback == null) {
                fallback = entity;
            }
            if (camera.active()) {
                active = entity;
                break;
            }
        }
        Entity chosen = active != null ? active : fallback;
        return Optional.ofNullable(chosen);
    }

    /**
     * Resolves the camera on a named entity; falls back to {@link #resolveForPass} when not found.
     */
    public static ActiveCamera resolveNamed(
            EntityStore entities,
            String entityName,
            String passTargetId,
            float surfaceWidth,
            float surfaceHeight) {
        if (entityName == null || entityName.isBlank()) {
            return resolveForPass(entities, passTargetId, surfaceWidth, surfaceHeight);
        }
        Entity entity = entities.findByName(entityName);
        if (entity == null) {
            System.err.println(
                    "Warning: UI pass camera entity '" + entityName + "' not found; using active camera.");
            return resolveForPass(entities, passTargetId, surfaceWidth, surfaceHeight);
        }
        Camera camera = entities.getComponent(entity.id(), Camera.class);
        Transform transform = entities.getComponent(entity.id(), Transform.class);
        if (camera == null || transform == null) {
            throw new IllegalStateException(
                    "Camera entity '"
                            + entityName
                            + "' must have Camera and Transform components on the same entity.");
        }
        float viewportWidth =
                camera.viewportWidth() > 0f ? camera.viewportWidth() : surfaceWidth;
        float viewportHeight =
                camera.viewportHeight() > 0f ? camera.viewportHeight() : surfaceHeight;
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
                viewportHeight,
                camera.fitMode(),
                camera.designAspect(),
                camera.lookAtX(),
                camera.lookAtY(),
                camera.lookAtZ(),
                camera.renderTarget());
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
                windowHeight,
                dev.hermes.api.ecs.ViewportFitMode.LETTERBOX,
                0f,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                null);
    }
}
