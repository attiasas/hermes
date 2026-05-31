package dev.hermes.core.world;

import dev.hermes.api.world.SceneCameraController;
import dev.hermes.api.world.SceneCameraConfig;
import dev.hermes.api.world.ActiveCameraView;
import dev.hermes.api.world.MainCameraBinding;
import java.util.Optional;

public class SceneCameraControllerImpl implements SceneCameraController {
    @Override
    public SceneCameraConfig sceneConfig() {
        return new SceneCameraConfig();
    }

    @Override
    public MainCameraBinding mainBinding() {
        return MainCameraBinding.SCENE;
    }
    
    @Override
    public Optional<String> mainEntityName() {
        return Optional.empty();
    }

    @Override
    public void bindMain(String entityName) {
        // TODO: Implement
    }
    
    @Override
    public void unbindMain() {
        // TODO: Implement
    }

    @Override
    public ActiveCameraView resolveMain(float surfaceWidth, float surfaceHeight) {
        // TODO: Implement
        return null;
    }
}
