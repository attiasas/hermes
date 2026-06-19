package dev.hermes.core.world;

import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.world.ActiveCameraView;
import dev.hermes.api.world.CameraControlsConfig;
import dev.hermes.api.world.MainCameraBinding;
import dev.hermes.api.world.SceneCameraConfig;
import dev.hermes.api.world.SceneCameraController;

import java.util.Optional;

public class SceneCameraControllerImpl implements SceneCameraController {

    private final WorldManager owner;
    private SceneCameraConfig sceneConfig = new SceneCameraConfig();
    private CameraControlsConfig controls = CameraControlsConfig.disabled();
    private boolean sceneConfigSet;
    private MainCameraBinding mainBinding = MainCameraBinding.SCENE;
    private String mainEntityName;

    public SceneCameraControllerImpl() {
        this(null);
    }

    public SceneCameraControllerImpl(WorldManager owner) {
        this.owner = owner;
    }

    @Override
    public SceneCameraConfig sceneConfig() {
        return sceneConfig;
    }

    @Override
    public void setSceneConfig(SceneCameraConfig config) {
        this.sceneConfig = config == null ? new SceneCameraConfig() : config;
        this.sceneConfigSet = true;
    }

    @Override
    public CameraControlsConfig controls() {
        return controls;
    }

    @Override
    public void setControls(CameraControlsConfig controls) {
        this.controls = controls == null ? CameraControlsConfig.disabled() : controls;
    }

    @Override
    public MainCameraBinding mainBinding() {
        return mainBinding;
    }

    @Override
    public Optional<String> mainEntityName() {
        return Optional.ofNullable(mainEntityName);
    }

    @Override
    public void bindMain(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            unbindMain();
            return;
        }
        this.mainBinding = MainCameraBinding.ENTITY;
        this.mainEntityName = entityName.trim();
    }

    @Override
    public void unbindMain() {
        this.mainBinding = MainCameraBinding.SCENE;
        this.mainEntityName = null;
    }

    @Override
    public ActiveCameraView resolveMain(float surfaceWidth, float surfaceHeight) {
        if (mainBinding == MainCameraBinding.ENTITY && mainEntityName != null && owner != null) {
            EntityStore entities = owner.entities();
            dev.hermes.api.Entity entity = entities.findByName(mainEntityName);
            if (entity != null) {
                Camera camera = entities.getComponent(entity.id(), Camera.class);
                Transform transform = entities.getComponent(entity.id(), Transform.class);
                if (camera != null && transform != null) {
                    float viewportWidth =
                            camera.viewportWidth() > 0f ? camera.viewportWidth() : surfaceWidth;
                    float viewportHeight =
                            camera.viewportHeight() > 0f ? camera.viewportHeight() : surfaceHeight;
                    return entityView(camera, transform, viewportWidth, viewportHeight);
                }
            }
        }
        if (!sceneConfigSet && mainBinding == MainCameraBinding.SCENE) {
            return null;
        }
        float viewportWidth =
                sceneConfig.viewportWidth() > 0f ? sceneConfig.viewportWidth() : surfaceWidth;
        float viewportHeight =
                sceneConfig.viewportHeight() > 0f ? sceneConfig.viewportHeight() : surfaceHeight;
        return ActiveCameraView.fromSceneConfig(sceneConfig, viewportWidth, viewportHeight);
    }

    private static ActiveCameraView entityView(
            Camera camera, Transform transform, float viewportWidth, float viewportHeight) {
        SceneCameraConfig.Projection projection =
                camera.projection() == Camera.Projection.PERSPECTIVE
                        ? SceneCameraConfig.Projection.PERSPECTIVE
                        : SceneCameraConfig.Projection.ORTHOGRAPHIC;
        return new ActiveCameraView(
                projection,
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
                camera.lookAtZ());
    }
}
