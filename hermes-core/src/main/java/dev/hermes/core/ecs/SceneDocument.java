package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import dev.hermes.api.scene.SceneAudioConfig;
import dev.hermes.api.scene.SceneUiConfig;
import dev.hermes.core.lighting.SceneLightingBlock;
import dev.hermes.core.resource.ScenePreloadSpec;
import dev.hermes.core.world.WorldBlock;
import dev.hermes.core.world.WorldBlockParser;
import dev.hermes.core.world.SceneCameraBlock;
import dev.hermes.core.world.SceneCameraBlockParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses scene JSON into entity specifications and optional scene metadata.
 */
final class SceneDocument {

    private final List<EntitySpec> entities;
    private final String renderPipeline;
    private final String inputContext;
    private final SceneUiConfig uiConfig;
    private final SceneAudioConfig audioConfig;
    private final Optional<SceneLightingBlock> lighting;
    private final Optional<ScenePreloadSpec> preload;
    private final Optional<WorldBlock> world;
    private final Optional<SceneCameraBlock> camera;

    private SceneDocument(
            List<EntitySpec> entities,
            String renderPipeline,
            String inputContext,
            SceneUiConfig uiConfig,
            SceneAudioConfig audioConfig,
            Optional<SceneLightingBlock> lighting,
            Optional<ScenePreloadSpec> preload,
            Optional<WorldBlock> world,
            Optional<SceneCameraBlock> camera) {
        this.entities = entities;
        this.renderPipeline = renderPipeline;
        this.inputContext = inputContext;
        this.uiConfig = uiConfig;
        this.audioConfig = audioConfig;
        this.lighting = lighting;
        this.preload = preload;
        this.world = world;
        this.camera = camera;
    }

    static SceneDocument parse(String scenePath, String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            JsonValue entitiesArray = root.get("entities");
            List<EntitySpec> entities = new ArrayList<>();
            if (entitiesArray != null && entitiesArray.isArray()) {
                for (int i = 0; i < entitiesArray.size; i++) {
                    JsonValue entityValue = entitiesArray.get(i);
                    if (!entityValue.isObject()) {
                        throw new SceneParseException(
                                "Scene '" + scenePath + "': entities[" + i + "] must be an object.");
                    }
                    String id = entityValue.has("id") ? entityValue.getString("id", "") : "";
                    String kind = "";
                    if (entityValue.has("type")) {
                        kind = entityValue.getString("type", "");
                    } else if (entityValue.has("kind")) {
                        kind = entityValue.getString("kind", "");
                    }
                    JsonValue componentsValue = entityValue.get("components");
                    List<ComponentSpec> components = new ArrayList<>();
                    if (componentsValue != null && componentsValue.isObject()) {
                        for (JsonValue componentEntry : componentsValue) {
                            String typeName = componentEntry.name;
                            JsonValue properties = componentEntry.isObject() ? componentEntry : new JsonValue(JsonValue.ValueType.object);
                            components.add(new ComponentSpec(typeName, properties));
                        }
                    }
                    entities.add(new EntitySpec(id, kind, components));
                }
            }
            String renderPipeline = null;
            if (root.has("renderPipeline")) {
                renderPipeline = root.getString("renderPipeline", "").trim();
                if (renderPipeline.isEmpty()) {
                    throw new SceneParseException(
                            "Scene '" + scenePath + "': \"renderPipeline\" must be non-empty when set.");
                }
            }
            String inputContext = null;
            if (root.has("inputContext")) {
                inputContext = root.getString("inputContext", "").trim();
                if (inputContext.isEmpty()) {
                    throw new SceneParseException(
                            "Scene '" + scenePath + "': \"inputContext\" must be non-empty when set.");
                }
            }
            SceneUiConfig uiConfig = null;
            if (root.has("ui")) {
                uiConfig = parseUiConfig(scenePath, root.get("ui"));
            }
            SceneAudioConfig audioConfig = null;
            if (root.has("audio")) {
                audioConfig = parseAudioConfig(scenePath, root.get("audio"));
            }
            Optional<SceneLightingBlock> lighting = Optional.empty();
            if (root.has("lighting")) {
                lighting = Optional.of(parseLighting(scenePath, root.get("lighting")));
            }
            Optional<ScenePreloadSpec> preload = Optional.empty();
            if (root.has("preload")) {
                preload = Optional.of(ScenePreloadSpec.parse(scenePath, root.get("preload")));
            }
            Optional<WorldBlock> world = Optional.empty();
            if (root.has("world")) {
                world = Optional.of(
                        WorldBlockParser.parse(
                                scenePath, root.get("world"), Optional.ofNullable(uiConfig)));
            }
            Optional<SceneCameraBlock> camera = Optional.empty();
            if (root.has("camera")) {
                camera = Optional.of(SceneCameraBlockParser.parse(scenePath, root.get("camera")));
            }
            return new SceneDocument(
                    entities,
                    renderPipeline,
                    inputContext,
                    uiConfig,
                    audioConfig,
                    lighting,
                    preload,
                    world,
                    camera);
        } catch (SceneParseException e) {
            throw e;
        } catch (Exception e) {
            throw new SceneLoadException("Scene '" + scenePath + "': invalid JSON: " + e.getMessage(), e);
        }
    }

    List<EntitySpec> entities() {
        return entities;
    }

    Optional<String> renderPipeline() {
        return Optional.ofNullable(renderPipeline);
    }

    Optional<String> inputContext() {
        return Optional.ofNullable(inputContext);
    }

    Optional<SceneUiConfig> uiConfig() {
        return Optional.ofNullable(uiConfig);
    }

    Optional<SceneAudioConfig> audioConfig() {
        return Optional.ofNullable(audioConfig);
    }

    Optional<SceneLightingBlock> lighting() {
        return lighting;
    }

    Optional<ScenePreloadSpec> preload() {
        return preload;
    }

    Optional<WorldBlock> world() {
        return world;
    }

    Optional<SceneCameraBlock> camera() {
        return camera;
    }

    private static SceneAudioConfig parseAudioConfig(String scenePath, JsonValue audioValue) {
        if (audioValue == null || !audioValue.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"audio\" must be an object.");
        }
        String bgm = audioValue.getString("bgm", "").trim();
        String bgmPlaylist = audioValue.getString("bgmPlaylist", "").trim();
        float fadeInSeconds = audioValue.getFloat("fadeInSeconds", 1f);
        float fadeOutSeconds = audioValue.getFloat("fadeOutSeconds", 1f);
        boolean pauseBgmOnPause = audioValue.getBoolean("pauseBgmOnPause", false);
        return new SceneAudioConfig(bgm, bgmPlaylist, fadeInSeconds, fadeOutSeconds, pauseBgmOnPause);
    }

    private static SceneLightingBlock parseLighting(String scenePath, JsonValue lightingValue) {
        if (lightingValue == null || !lightingValue.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"lighting\" must be an object.");
        }
        int version = lightingValue.getInt("version", -1);
        if (version != 1) {
            throw new SceneParseException("Scene '" + scenePath + "': \"lighting.version\" must be 1.");
        }
        Optional<SceneLightingBlock.AmbientEntry> ambient = Optional.empty();
        if (lightingValue.has("ambient")) {
            ambient = Optional.of(parseAmbient(scenePath, lightingValue.get("ambient")));
        }
        Optional<SceneLightingBlock.DirectionalEntry> directional = Optional.empty();
        if (lightingValue.has("directional")) {
            directional = Optional.of(parseDirectional(scenePath, lightingValue.get("directional")));
        }
        List<SceneLightingBlock.PointLightEntry> pointLights =
                parsePointLightArray(scenePath, lightingValue.get("point"));
        List<SceneLightingBlock.SpotLightEntry> spotLights =
                parseSpotLightArray(scenePath, lightingValue.get("spot"));
        return new SceneLightingBlock(ambient, directional, pointLights, spotLights);
    }

    private static SceneLightingBlock.AmbientEntry parseAmbient(String scenePath, JsonValue value) {
        if (value == null || !value.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"lighting.ambient\" must be an object.");
        }
        float[] color = parseColor(scenePath, "lighting.ambient", value.get("color"));
        float intensity = value.getFloat("intensity", 1f);
        return new SceneLightingBlock.AmbientEntry(color, intensity);
    }

    private static SceneLightingBlock.DirectionalEntry parseDirectional(String scenePath, JsonValue value) {
        if (value == null || !value.isObject()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"lighting.directional\" must be an object.");
        }
        float[] color = parseColor(scenePath, "lighting.directional", value.get("color"));
        float intensity = value.getFloat("intensity", 1f);
        float[] direction = parseDirection(scenePath, "lighting.directional", value.get("direction"));
        return new SceneLightingBlock.DirectionalEntry(color, intensity, direction);
    }

    private static List<SceneLightingBlock.PointLightEntry> parsePointLightArray(
            String scenePath, JsonValue arrayValue) {
        if (arrayValue == null) {
            return List.of();
        }
        if (!arrayValue.isArray()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"lighting.point\" must be an array.");
        }
        List<SceneLightingBlock.PointLightEntry> entries = new ArrayList<>();
        for (int i = 0; i < arrayValue.size; i++) {
            JsonValue entry = arrayValue.get(i);
            if (entry == null || !entry.isObject()) {
                throw new SceneParseException(
                        "Scene '" + scenePath + "': \"lighting.point[" + i + "]\" must be an object.");
            }
            float[] position = parsePosition(scenePath, "lighting.point[" + i + "]", entry.get("position"));
            float[] color = parseColor(scenePath, "lighting.point[" + i + "]", entry.get("color"));
            float intensity = entry.getFloat("intensity", 1f);
            float range = entry.getFloat("range", 10f);
            entries.add(new SceneLightingBlock.PointLightEntry(position, color, intensity, range));
        }
        return entries;
    }

    private static List<SceneLightingBlock.SpotLightEntry> parseSpotLightArray(
            String scenePath, JsonValue arrayValue) {
        if (arrayValue == null) {
            return List.of();
        }
        if (!arrayValue.isArray()) {
            throw new SceneParseException("Scene '" + scenePath + "': \"lighting.spot\" must be an array.");
        }
        List<SceneLightingBlock.SpotLightEntry> entries = new ArrayList<>();
        for (int i = 0; i < arrayValue.size; i++) {
            JsonValue entry = arrayValue.get(i);
            if (entry == null || !entry.isObject()) {
                throw new SceneParseException(
                        "Scene '" + scenePath + "': \"lighting.spot[" + i + "]\" must be an object.");
            }
            float[] position = parsePosition(scenePath, "lighting.spot[" + i + "]", entry.get("position"));
            float[] color = parseColor(scenePath, "lighting.spot[" + i + "]", entry.get("color"));
            float intensity = entry.getFloat("intensity", 1f);
            float range = entry.getFloat("range", 10f);
            float[] direction = parseDirection(scenePath, "lighting.spot[" + i + "]", entry.get("direction"));
            float cutoffAngle = entry.getFloat("cutoffAngle", 45f);
            float exponent = entry.getFloat("exponent", 1f);
            entries.add(
                    new SceneLightingBlock.SpotLightEntry(
                            position, color, intensity, range, direction, cutoffAngle, exponent));
        }
        return entries;
    }

    private static float[] parseColor(String scenePath, String fieldPath, JsonValue value) {
        float[] color = toFloatArray(value);
        if (color.length != 3 && color.length != 4) {
            throw new SceneParseException(
                    "Scene '" + scenePath + "': \"" + fieldPath + ".color\" must be a 3- or 4-element array.");
        }
        return color;
    }

    private static float[] parsePosition(String scenePath, String fieldPath, JsonValue value) {
        float[] position = toFloatArray(value);
        if (position.length < 3) {
            throw new SceneParseException(
                    "Scene '" + scenePath + "': \"" + fieldPath + ".position\" must be a 3-element array.");
        }
        return position;
    }

    private static float[] parseDirection(String scenePath, String fieldPath, JsonValue value) {
        if (value == null) {
            return new float[0];
        }
        float[] direction = toFloatArray(value);
        if (direction.length < 3) {
            throw new SceneParseException(
                    "Scene '" + scenePath + "': \"" + fieldPath + ".direction\" must be a 3-element array.");
        }
        return direction;
    }

    private static float[] toFloatArray(JsonValue value) {
        if (value == null || value.isNull()) {
            return new float[0];
        }
        if (value.isArray()) {
            float[] arr = new float[value.size];
            for (int i = 0; i < value.size; i++) {
                arr[i] = value.getFloat(i);
            }
            return arr;
        }
        if (value.isNumber()) {
            return new float[] {value.asFloat()};
        }
        return new float[0];
    }

    private static SceneUiConfig parseUiConfig(String scenePath, JsonValue uiValue) {
        if (uiValue == null) {
            throw new SceneParseException("Scene '" + scenePath + "': \"ui\" must be a string or object.");
        }
        if (uiValue.isString()) {
            String document = uiValue.asString().trim();
            if (document.isEmpty()) {
                throw new SceneParseException(
                        "Scene '" + scenePath + "': \"ui\" document path must be non-empty.");
            }
            return new SceneUiConfig(document);
        }
        if (uiValue.isObject()) {
            String document = uiValue.getString("document", "").trim();
            if (document.isEmpty()) {
                throw new SceneParseException(
                        "Scene '" + scenePath + "': \"ui.document\" must be non-empty when \"ui\" is an object.");
            }
            String fitMode = uiValue.has("fitMode") ? uiValue.getString("fitMode", "") : null;
            Float designAspect = uiValue.has("designAspect") ? uiValue.getFloat("designAspect", 0f) : null;
            return new SceneUiConfig(document, fitMode, designAspect);
        }
        throw new SceneParseException("Scene '" + scenePath + "': \"ui\" must be a string or object.");
    }

    static final class EntitySpec {
        private final String id;
        private final String kind;
        private final List<ComponentSpec> components;

        EntitySpec(String id, String kind, List<ComponentSpec> components) {
            this.id = id;
            this.kind = kind;
            this.components = components;
        }

        String id() {
            return id;
        }

        String kind() {
            return kind;
        }

        List<ComponentSpec> components() {
            return components;
        }
    }

    static final class ComponentSpec {
        private final String typeName;
        private final JsonValue properties;

        ComponentSpec(String typeName, JsonValue properties) {
            this.typeName = typeName;
            this.properties = properties;
        }

        String typeName() {
            return typeName;
        }

        JsonValue properties() {
            return properties;
        }
    }
}
