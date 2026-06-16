package dev.hermes.core.world;

import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.world.CameraControlsConfig;
import dev.hermes.api.world.CameraControlsMode;
import dev.hermes.api.world.SceneCameraConfig;
import dev.hermes.core.ecs.SceneParseException;

import com.badlogic.gdx.utils.JsonValue;

import java.util.Locale;
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
        CameraControlsConfig controls = parseControls(scenePath, cameraValue, config.projection());
        return new SceneCameraBlock(config, controls, follow);
    }

    private static CameraControlsConfig parseControls(
            String scenePath, JsonValue cameraValue, SceneCameraConfig.Projection projection) {
        JsonValue controlsValue = cameraValue.get("controls");
        if (controlsValue == null || controlsValue.isNull()) {
            return projection == SceneCameraConfig.Projection.PERSPECTIVE
                    ? CameraControlsConfig.orbitDefaults()
                    : CameraControlsConfig.disabled();
        }
        if (!controlsValue.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"camera.controls\" must be an object.");
        }
        CameraControlsConfig controls =
                projection == SceneCameraConfig.Projection.PERSPECTIVE
                        ? CameraControlsConfig.orbitDefaults()
                        : CameraControlsConfig.disabled();
        controls.setMode(parseControlsMode(scenePath, controlsValue.getString("mode", "orbit")));
        if (controlsValue.has("enabled")) {
            controls.setEnabled(controlsValue.getBoolean("enabled", controls.enabled()));
        }
        if (controlsValue.has("rotateButton")) {
            controls.setRotateButton(parseButton(scenePath, controlsValue.getString("rotateButton"), "rotateButton"));
        }
        if (controlsValue.has("translateButton")) {
            controls.setTranslateButton(
                    parseButton(scenePath, controlsValue.getString("translateButton"), "translateButton"));
        }
        if (controlsValue.has("forwardButton")) {
            controls.setForwardButton(
                    parseButton(scenePath, controlsValue.getString("forwardButton"), "forwardButton"));
        }
        controls.setRotateAngle(controlsValue.getFloat("rotateAngle", controls.rotateAngle()));
        controls.setTranslateUnits(controlsValue.getFloat("translateUnits", controls.translateUnits()));
        controls.setScrollFactor(controlsValue.getFloat("scrollFactor", controls.scrollFactor()));
        controls.setScrollZoom(controlsValue.getBoolean("scrollZoom", controls.scrollZoom()));
        controls.setTranslateTarget(controlsValue.getBoolean("translateTarget", controls.translateTarget()));
        controls.setForwardTarget(controlsValue.getBoolean("forwardTarget", controls.forwardTarget()));
        controls.setScrollTarget(controlsValue.getBoolean("scrollTarget", controls.scrollTarget()));
        controls.setVelocity(controlsValue.getFloat("velocity", controls.velocity()));
        controls.setDegreesPerPixel(controlsValue.getFloat("degreesPerPixel", controls.degreesPerPixel()));
        return controls;
    }

    private static CameraControlsMode parseControlsMode(String scenePath, String value) {
        if (value == null || value.isBlank()) {
            return CameraControlsMode.ORBIT;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("orbit".equals(normalized)) {
            return CameraControlsMode.ORBIT;
        }
        if ("firstperson".equals(normalized) || "first_person".equals(normalized)) {
            return CameraControlsMode.FIRST_PERSON;
        }
        throw new SceneParseException(
                "Scene '" + scenePath + "': \"camera.controls.mode\" must be \"orbit\" or \"firstPerson\".");
    }

    private static int parseButton(String scenePath, String value, String field) {
        try {
            return InputButton.byName(value);
        } catch (IllegalArgumentException ex) {
            throw new SceneParseException(
                    "Scene '" + scenePath + "': \"camera.controls." + field + "\" " + ex.getMessage());
        }
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
