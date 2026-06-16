package dev.hermes.core.world;

import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.world.SceneCameraConfig;
import dev.hermes.core.ecs.SceneParseException;

import com.badlogic.gdx.utils.JsonValue;

import java.util.Optional;

/** Parses scene JSON {@code "camera"} block (version 1). */
public final class SceneCameraBlockParser {

    private SceneCameraBlockParser() {}

    public static SceneCameraBlock parse(String scenePath, JsonValue cameraValue) {
        if (cameraValue == null || cameraValue.isNull()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"camera\" is required for parse.");
        }
        if (!cameraValue.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"camera\" must be an object.");
        }
        int version = cameraValue.getInt("version", -1);
        if (version != 1) {
            throw new SceneParseException("Scene '" + scenePath + "': \"camera.version\" must be 1.");
        }
        SceneCameraConfig config = new SceneCameraConfig();
        config.setProjection(parseProjection(scenePath, cameraValue.getString("projection", "orthographic")));
        config.setX(cameraValue.getFloat("x", 0f));
        config.setY(cameraValue.getFloat("y", 0f));
        config.setZ(cameraValue.getFloat("z", 0f));
        config.setRotationX(cameraValue.getFloat("rotationX", 0f));
        config.setRotationY(cameraValue.getFloat("rotationY", 0f));
        config.setRotationZ(cameraValue.getFloat("rotationZ", 0f));
        config.setZoom(cameraValue.getFloat("zoom", 1f));
        config.setFieldOfView(cameraValue.getFloat("fieldOfView", 67f));
        config.setNear(cameraValue.getFloat("near", 0.1f));
        config.setFar(cameraValue.getFloat("far", 3000f));
        config.setViewportWidth(cameraValue.getFloat("viewportWidth", 0f));
        config.setViewportHeight(cameraValue.getFloat("viewportHeight", 0f));
        config.setFitMode(parseFitMode(cameraValue.getString("fitMode", "letterbox")));
        config.setDesignAspect(cameraValue.getFloat("designAspect", 0f));
        JsonValue lookAt = cameraValue.get("lookAt");
        if (lookAt != null && lookAt.isObject()) {
            config.setLookAt(
                    lookAt.getFloat("x", 0f), lookAt.getFloat("y", 0f), lookAt.getFloat("z", 0f));
        }
        Optional<String> follow = Optional.empty();
        if (cameraValue.has("follow")) {
            JsonValue followValue = cameraValue.get("follow");
            if (followValue != null && !followValue.isNull()) {
                String name = followValue.asString();
                if (name != null && !name.isBlank()) {
                    follow = Optional.of(name.trim());
                }
            }
        }
        return new SceneCameraBlock(config, follow);
    }

    private static SceneCameraConfig.Projection parseProjection(String scenePath, String value) {
        if (value == null) {
            return SceneCameraConfig.Projection.ORTHOGRAPHIC;
        }
        String normalized = value.trim().toLowerCase();
        if ("perspective".equals(normalized) || "3d".equals(normalized)) {
            return SceneCameraConfig.Projection.PERSPECTIVE;
        }
        if ("orthographic".equals(normalized) || "2d".equals(normalized) || normalized.isEmpty()) {
            return SceneCameraConfig.Projection.ORTHOGRAPHIC;
        }
        throw new SceneParseException(
                "Scene '" + scenePath + "': \"camera.projection\" must be \"orthographic\" or \"perspective\".");
    }

    private static ViewportFitMode parseFitMode(String value) {
        if (value == null || value.isBlank()) {
            return ViewportFitMode.LETTERBOX;
        }
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "stretch":
                return ViewportFitMode.STRETCH;
            case "fit":
            case "letterbox":
                return ViewportFitMode.LETTERBOX;
            case "crop":
                return ViewportFitMode.CROP;
            default:
                return ViewportFitMode.LETTERBOX;
        }
    }
}
