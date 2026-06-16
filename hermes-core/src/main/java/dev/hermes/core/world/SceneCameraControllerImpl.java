package dev.hermes.core.world;

import dev.hermes.api.world.ActiveCameraView;
import dev.hermes.api.world.MainCameraBinding;
import dev.hermes.api.world.SceneCameraConfig;
import dev.hermes.api.world.SceneCameraController;

import java.util.Optional;

public class SceneCameraControllerImpl implements SceneCameraController {

    private SceneCameraConfig sceneConfig = new SceneCameraConfig();
    private MainCameraBinding mainBinding = MainCameraBinding.SCENE;
    private String mainEntityName;

    @Override
    public SceneCameraConfig sceneConfig() {
        return sceneConfig;
    }

    @Override
    public void setSceneConfig(SceneCameraConfig config) {
        this.sceneConfig = config == null ? new SceneCameraConfig() : config;
        if (mainBinding == MainCameraBinding.SCENE) {
            // keep SCENE binding when config updated at load
        }
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
        float viewportWidth =
                sceneConfig.viewportWidth() > 0f ? sceneConfig.viewportWidth() : surfaceWidth;
        float viewportHeight =
                sceneConfig.viewportHeight() > 0f ? sceneConfig.viewportHeight() : surfaceHeight;
        return ActiveCameraView.fromSceneConfig(sceneConfig, viewportWidth, viewportHeight);
    }
}
