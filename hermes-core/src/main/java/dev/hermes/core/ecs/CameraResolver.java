package dev.hermes.core.ecs;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.world.ActiveCameraView;
import dev.hermes.api.world.MainCameraBinding;
import dev.hermes.api.world.SceneCameraConfig;

import java.util.Optional;

/** Selects the active scene camera from scene config, bound entities, or auxiliary pass cameras. */
public final class CameraResolver {

    private CameraResolver() {}

    public static ActiveCamera resolveForManager(
            WorldManager manager, String passTargetId, float surfaceWidth, float surfaceHeight) {
        if (manager == null) {
            return defaultCamera(surfaceWidth, surfaceHeight);
        }
        boolean matchTarget =
                passTargetId != null && !passTargetId.isBlank() && !"screen".equals(passTargetId);
        if (matchTarget) {
            Optional<ActiveCamera> renderTarget = resolveRenderTargetCamera(
                    manager.entities(), passTargetId, surfaceWidth, surfaceHeight);
            if (renderTarget.isPresent()) {
                return renderTarget.get();
            }
            System.err.println(
                    "Warning: no Camera with renderTarget '"
                            + passTargetId
                            + "'; using main camera.");
        }
        return resolveMainCamera(manager, surfaceWidth, surfaceHeight);
    }

    public static ActiveCamera resolveForPass(
            EntityStore entities, String passTargetId, float surfaceWidth, float surfaceHeight) {
        if (entities == null) {
            return defaultCamera(surfaceWidth, surfaceHeight);
        }
        WorldManager manager = WorldManagerRegistry.lookup(entities);
        if (manager != null) {
            return resolveForManager(manager, passTargetId, surfaceWidth, surfaceHeight);
        }
        boolean matchTarget =
                passTargetId != null && !passTargetId.isBlank() && !"screen".equals(passTargetId);
        if (matchTarget) {
            return resolveRenderTargetCamera(entities, passTargetId, surfaceWidth, surfaceHeight)
                    .orElseGet(() -> defaultCamera(surfaceWidth, surfaceHeight));
        }
        ActiveCamera legacy = resolveLegacyEntityMain(entities, surfaceWidth, surfaceHeight);
        return legacy != null ? legacy : defaultCamera(surfaceWidth, surfaceHeight);
    }

    public static ActiveCamera resolve(EntityStore entities, float windowWidth, float windowHeight) {
        return resolveForPass(entities, "screen", windowWidth, windowHeight);
    }

    /** Main camera entity when bound via {@link dev.hermes.api.world.SceneCameraController#bindMain}. */
    public static Optional<Entity> mainCameraEntity(WorldManager manager) {
        if (manager == null || manager.camera().mainBinding() != MainCameraBinding.ENTITY) {
            return Optional.empty();
        }
        return manager.camera().mainEntityName().flatMap(name -> Optional.ofNullable(
                manager.entities().findByName(name)));
    }

    /** @deprecated use {@link #mainCameraEntity(WorldManager)} */
    @Deprecated
    public static Optional<Entity> activeCameraEntity(EntityStore entities) {
        WorldManager manager = WorldManagerRegistry.lookup(entities);
        if (manager != null) {
            return mainCameraEntity(manager);
        }
        for (Entity entity : entities.entitiesWith(Camera.class)) {
            Camera camera = entities.getComponent(entity.id(), Camera.class);
            if (camera == null || entities.getComponent(entity.id(), Transform.class) == null) {
                continue;
            }
            return Optional.of(entity);
        }
        return Optional.empty();
    }

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
                    "Warning: camera entity '" + entityName + "' not found; using main camera.");
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

    private static ActiveCamera resolveMainCamera(
            WorldManager manager, float surfaceWidth, float surfaceHeight) {
        if (manager.camera().mainBinding() == MainCameraBinding.ENTITY) {
            Optional<String> name = manager.camera().mainEntityName();
            if (name.isPresent()) {
                Entity entity = manager.entities().findByName(name.get());
                if (entity != null) {
                    Camera camera = manager.entities().getComponent(entity.id(), Camera.class);
                    Transform transform = manager.entities().getComponent(entity.id(), Transform.class);
                    if (camera != null && transform != null) {
                        float viewportWidth =
                                camera.viewportWidth() > 0f ? camera.viewportWidth() : surfaceWidth;
                        float viewportHeight =
                                camera.viewportHeight() > 0f ? camera.viewportHeight() : surfaceHeight;
                        return fromComponents(transform, camera, viewportWidth, viewportHeight);
                    }
                }
            }
        }
        ActiveCameraView view = manager.camera().resolveMain(surfaceWidth, surfaceHeight);
        if (view != null) {
            return fromView(view);
        }
        ActiveCamera legacy = resolveLegacyEntityMain(manager.entities(), surfaceWidth, surfaceHeight);
        if (legacy != null) {
            return legacy;
        }
        return defaultCamera(surfaceWidth, surfaceHeight);
    }

    private static Optional<ActiveCamera> resolveRenderTargetCamera(
            EntityStore entities, String passTargetId, float surfaceWidth, float surfaceHeight) {
        for (Entity entity : entities.entitiesWith(Camera.class)) {
            Camera camera = entities.getComponent(entity.id(), Camera.class);
            if (camera == null || !passTargetId.equals(camera.renderTarget())) {
                continue;
            }
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (transform == null) {
                throw new IllegalStateException(
                        "Camera entity '"
                                + entity.name()
                                + "' must have a Transform component on the same entity.");
            }
            float viewportWidth =
                    camera.viewportWidth() > 0f ? camera.viewportWidth() : surfaceWidth;
            float viewportHeight =
                    camera.viewportHeight() > 0f ? camera.viewportHeight() : surfaceHeight;
            return Optional.of(fromComponents(transform, camera, viewportWidth, viewportHeight));
        }
        return Optional.empty();
    }

    private static ActiveCamera resolveLegacyEntityMain(
            EntityStore entities, float surfaceWidth, float surfaceHeight) {
        if (entities == null) {
            return null;
        }
        Entity fallback = null;
        Camera fallbackCamera = null;
        for (Entity entity : entities.entitiesWith(Camera.class)) {
            Camera camera = entities.getComponent(entity.id(), Camera.class);
            if (camera == null || entities.getComponent(entity.id(), Transform.class) == null) {
                continue;
            }
            if (camera.renderTarget() != null) {
                continue;
            }
            fallback = entity;
            fallbackCamera = camera;
            break;
        }
        if (fallback == null || fallbackCamera == null) {
            return null;
        }
        Transform transform = entities.getComponent(fallback.id(), Transform.class);
        float viewportWidth =
                fallbackCamera.viewportWidth() > 0f ? fallbackCamera.viewportWidth() : surfaceWidth;
        float viewportHeight =
                fallbackCamera.viewportHeight() > 0f ? fallbackCamera.viewportHeight() : surfaceHeight;
        return fromComponents(transform, fallbackCamera, viewportWidth, viewportHeight);
    }

    static ActiveCamera fromView(ActiveCameraView view) {
        return new ActiveCamera(
                toCameraProjection(view.projection()),
                view.x(),
                view.y(),
                view.z(),
                view.rotationX(),
                view.rotationY(),
                view.rotationZ(),
                view.zoom(),
                view.fieldOfView(),
                view.near(),
                view.far(),
                view.viewportWidth(),
                view.viewportHeight(),
                view.fitMode(),
                view.designAspect(),
                view.lookAtX(),
                view.lookAtY(),
                view.lookAtZ(),
                null);
    }

    private static Camera.Projection toCameraProjection(SceneCameraConfig.Projection projection) {
        return projection == SceneCameraConfig.Projection.PERSPECTIVE
                ? Camera.Projection.PERSPECTIVE
                : Camera.Projection.ORTHOGRAPHIC;
    }

    static ActiveCamera fromComponents(
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
                ViewportFitMode.LETTERBOX,
                0f,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                null);
    }
}
