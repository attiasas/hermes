package dev.hermes.api.world;

import java.util.Optional;

public interface SceneCameraController {
    SceneCameraConfig sceneConfig();
    void setSceneConfig(SceneCameraConfig config);

    CameraControlsConfig controls();
    void setControls(CameraControlsConfig controls);

    MainCameraBinding mainBinding(); // SCENE | ENTITY
    Optional<String> mainEntityName();

    void bindMain(String entityName);
    void unbindMain();
    ActiveCameraView resolveMain(float surfaceWidth, float surfaceHeight);
}
