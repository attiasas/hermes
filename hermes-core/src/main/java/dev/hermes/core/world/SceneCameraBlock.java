package dev.hermes.core.world;

import dev.hermes.api.world.CameraControlsConfig;
import dev.hermes.api.world.SceneCameraConfig;

import java.util.Optional;

/** Parsed scene JSON {@code "camera"} block (version 1). */
public final class SceneCameraBlock {

    private final SceneCameraConfig config;
    private final CameraControlsConfig controls;
    private final Optional<String> followEntity;

    public SceneCameraBlock(
            SceneCameraConfig config, CameraControlsConfig controls, Optional<String> followEntity) {
        this.config = config;
        this.controls = controls == null ? CameraControlsConfig.orbitDefaults() : controls;
        this.followEntity = followEntity == null ? Optional.empty() : followEntity;
    }

    public SceneCameraConfig config() {
        return config;
    }

    public CameraControlsConfig controls() {
        return controls;
    }

    public Optional<String> followEntity() {
        return followEntity;
    }
}
