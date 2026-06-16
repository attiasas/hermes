package dev.hermes.core.world;

import dev.hermes.api.scene.SceneUiConfig;
import dev.hermes.api.world.WorldBounds;
import dev.hermes.core.ecs.SceneParseException;

import com.badlogic.gdx.utils.JsonValue;

import java.util.Optional;

/** Resolves {@link WorldBounds} from scene world dimensions JSON and optional UI config. */
final class WorldBoundsResolver {

    private static final float DEFAULT_MATCH_WIDTH = 1280f;
    private static final float DEFAULT_MATCH_HEIGHT = 720f;

    private WorldBoundsResolver() {}

    static WorldBounds resolve(String scenePath, JsonValue dimensionsValue, Optional<SceneUiConfig> uiConfig) {
        if (dimensionsValue == null || dimensionsValue.isNull()) {
            return WorldBounds.unbounded();
        }
        if (!dimensionsValue.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"world.dimensions\" must be an object.");
        }
        if (dimensionsValue.has("match")) {
            String match = dimensionsValue.getString("match", "").trim();
            return resolveMatch(scenePath, match, uiConfig);
        }
        Float width = dimensionsValue.has("width") ? dimensionsValue.getFloat("width") : null;
        Float height = dimensionsValue.has("height") ? dimensionsValue.getFloat("height") : null;
        Float depth = dimensionsValue.has("depth") ? dimensionsValue.getFloat("depth") : null;
        if (width == null && height == null && depth == null) {
            return WorldBounds.unbounded();
        }
        String origin = dimensionsValue.getString("origin", "bottomLeft").trim();
        float w = width != null ? width : DEFAULT_MATCH_WIDTH;
        float h = height != null ? height : DEFAULT_MATCH_HEIGHT;
        float minZ = 0f;
        float maxZ = depth != null ? depth : Float.POSITIVE_INFINITY;
        float minZClamped = depth != null ? minZ : Float.NEGATIVE_INFINITY;
        if ("center".equalsIgnoreCase(origin)) {
            return WorldBounds.bounded(-w * 0.5f, -h * 0.5f, minZClamped, w * 0.5f, h * 0.5f, maxZ);
        }
        if (!"bottomLeft".equalsIgnoreCase(origin) && !origin.isEmpty()) {
            throw new SceneParseException(
                    "Scene '" + scenePath + "': \"world.dimensions.origin\" must be \"bottomLeft\" or \"center\".");
        }
        return WorldBounds.bounded(0f, 0f, minZClamped, w, h, maxZ);
    }

    private static WorldBounds resolveMatch(
            String scenePath, String match, Optional<SceneUiConfig> uiConfig) {
        if ("designViewport".equals(match)) {
            float height = DEFAULT_MATCH_HEIGHT;
            float width = DEFAULT_MATCH_WIDTH;
            if (uiConfig.isPresent()) {
                Optional<Float> aspect = uiConfig.get().designAspect();
                if (aspect.isPresent() && aspect.get() > 0f) {
                    width = height * aspect.get();
                }
            }
            return WorldBounds.bounded(0f, 0f, Float.NEGATIVE_INFINITY, width, height, Float.POSITIVE_INFINITY);
        }
        if ("cameraViewport".equals(match)) {
            return resolveMatch(scenePath, "designViewport", uiConfig);
        }
        throw new SceneParseException(
                "Scene '"
                        + scenePath
                        + "': \"world.dimensions.match\" must be \"designViewport\" or \"cameraViewport\".");
    }
}
