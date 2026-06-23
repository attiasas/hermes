package dev.hermes.core.ecs;

import dev.hermes.api.ecs.AmbientLight;
import dev.hermes.api.ecs.AmbientSource;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.animation.AnimationClipType;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.ComponentData;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.DirectionalLight;
import dev.hermes.api.ecs.PointLight;
import dev.hermes.api.ecs.SpotLight;
import dev.hermes.api.ecs.FootstepEmitter;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.SoundEmitter;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.MaterialUniform;
import dev.hermes.api.ecs.DrawableKind;
import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.DrawableRig;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.LocalTransform;
import dev.hermes.api.ecs.PartMaterial;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Selectable;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.ecs.SpriteSheet;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.UiAttach;
import dev.hermes.api.input.PickLayer;
import dev.hermes.api.audio.AudioBus;
import dev.hermes.api.ecs.ComponentContext;
import dev.hermes.api.resource.ResourceService;
import dev.hermes.core.audio.AmbientAudioSystem;
import dev.hermes.core.audio.AudioActionSystem;
import dev.hermes.core.audio.AudioServiceImpl;
import dev.hermes.core.audio.FootstepSystem;
import dev.hermes.core.audio.SoundEmitterSystem;
import dev.hermes.core.animation.AnimationSystem;
import dev.hermes.core.input.CameraControlSystem;
import dev.hermes.core.input.EntityDragSystem;
import dev.hermes.core.input.SelectionSystem;
import dev.hermes.core.lighting.BuiltinLightingSystem;
import dev.hermes.core.render.resource.PrimitiveModelDocument;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.ui.UiAttachSystem;
import dev.hermes.core.ui.UiInputSystem;
import dev.hermes.core.ui.UiServiceImpl;

import dev.hermes.api.ecs.TileMap;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BuiltinComponents {

    static final String TRANSFORM = "Transform";
    static final String DRAWABLES = "Drawables";
    static final String ANIMATION_CONTROLLER = "AnimationController";
    static final String TILE_MAP = "TileMap";
    static final String CAMERA = "Camera";
    static final String MATERIAL = "Material";
    static final String RENDER_LAYER = "RenderLayer";
    static final String SELECTABLE = "Selectable";
    static final String SELECTED = "Selected";
    static final String UI_ATTACH = "UiAttach";
    static final String AMBIENT_LIGHT = "AmbientLight";
    static final String DIRECTIONAL_LIGHT = "DirectionalLight";
    static final String POINT_LIGHT = "PointLight";
    static final String SPOT_LIGHT = "SpotLight";
    static final String AMBIENT_SOURCE = "AmbientSource";
    static final String SOUND_EMITTER = "SoundEmitter";
    static final String FOOTSTEP_EMITTER = "FootstepEmitter";

    private BuiltinComponents() {
    }

    public static void register(ComponentRegistry registry) {
        registry.register(
                TRANSFORM,
                Transform.class,
                (data, ctx) -> {
                    Transform transform = new Transform();
                    transform.setX(data.getFloat("x", 0f));
                    transform.setY(data.getFloat("y", 0f));
                    transform.setZ(data.getFloat("z", 0f));
                    transform.setRotationX(data.getFloat("rotationX", 0f));
                    transform.setRotationY(data.getFloat("rotationY", 0f));
                    transform.setRotationZ(data.getFloat("rotationZ", 0f));
                    transform.setScaleX(data.getFloat("scaleX", 1f));
                    transform.setScaleY(data.getFloat("scaleY", 1f));
                    transform.setScaleZ(data.getFloat("scaleZ", 1f));
                    return transform;
                });
        registry.register(
                DRAWABLES,
                Drawables.class,
                (data, ctx) -> {
                    if (!(data instanceof JsonComponentData)) {
                        return new Drawables(List.of());
                    }
                    JsonComponentData json = (JsonComponentData) data;
                    JsonValue root = json.value();
                    if (root.has("parts")) {
                        return new Drawables(parseDrawableParts(root.get("parts"), ctx));
                    }
                    if (root.has("sprite")) {
                        String texture = resolveResourcePath(root.getString("sprite", ""), ctx);
                        return Drawables.singleSprite(texture);
                    }
                    if (root.has("mesh")) {
                        DrawablePart part = DrawablePart.mesh("default", resolveResourcePath(root.getString("mesh", ""), ctx));
                        if (root.has("texture")) {
                            part.setTexture(resolveResourcePath(root.getString("texture", ""), ctx));
                        }
                        return new Drawables(List.of(part));
                    }
                    return new Drawables(List.of());
                });
        registry.register(
                ANIMATION_CONTROLLER,
                AnimationController.class,
                (data, ctx) -> parseAnimationController(data, ctx));
        registry.register(
                TILE_MAP,
                TileMap.class,
                (data, ctx) -> {
                    TileMap tileMap = new TileMap();
                    tileMap.setMap(resolveResourcePath(data.getString("map", ""), ctx));
                    tileMap.setLayer(data.getString("layer", "ground"));
                    return tileMap;
                });
        registry.register(
                CAMERA,
                Camera.class,
                (data, ctx) -> {
                    Camera camera = new Camera();
                    camera.setProjection(parseProjection(data.getString("projection", "orthographic")));
                    camera.setZoom(data.getFloat("zoom", 1f));
                    camera.setFieldOfView(data.getFloat("fieldOfView", 67f));
                    camera.setNear(data.getFloat("near", 0.1f));
                    camera.setFar(data.getFloat("far", 3000f));
                    camera.setViewportWidth(data.getFloat("viewportWidth", 0f));
                    camera.setViewportHeight(data.getFloat("viewportHeight", 0f));
                    if (data.has("renderTarget")) {
                        camera.setRenderTarget(data.getString("renderTarget", ""));
                    }
                    camera.setFitMode(parseFitMode(data.getString("fitMode", "letterbox")));
                    camera.setDesignAspect(data.getFloat("designAspect", 0f));
                    if (data instanceof JsonComponentData) {
                        JsonComponentData json = (JsonComponentData) data;
                        if (json.has("lookAt")) {
                            camera.setLookAt(
                                    json.getNestedFloat("lookAt", "x", 0f),
                                    json.getNestedFloat("lookAt", "y", 0f),
                                    json.getNestedFloat("lookAt", "z", 0f));
                        }
                    }
                    return camera;
                });
        registry.register(
                MATERIAL,
                Material.class,
                (data, ctx) -> {
                    Material material = new Material();
                    material.setShader(data.getString("shader", "default/unlit"));
                    if (data instanceof JsonComponentData) {
                        JsonComponentData json = (JsonComponentData) data;
                        Map<String, MaterialUniform> uniforms = new HashMap<>();
                        for (Map.Entry<String, float[]> entry : json.getFloatArrayMap("uniforms").entrySet()) {
                            uniforms.put(entry.getKey(), new MaterialUniform(entry.getValue()));
                        }
                        material.setUniforms(uniforms);
                    }
                    return material;
                });
        registry.register(
                RENDER_LAYER,
                RenderLayer.class,
                (data, ctx) -> {
                    RenderLayer renderLayer = new RenderLayer();
                    renderLayer.setLayer(parseRenderLayer(data.getString("layer", "WORLD")));
                    return renderLayer;
                });
        registry.register(
                SELECTABLE,
                Selectable.class,
                (data, ctx) -> {
                    Selectable selectable = new Selectable();
                    selectable.setEnabled(data.getBoolean("enabled", true));
                    selectable.setRadius(data.getFloat("radius", 16f));
                    selectable.setLayer(parsePickLayer(data.getString("layer", "WORLD")));
                    return selectable;
                });
        registry.register(SELECTED, Selected.class, (data, ctx) -> new Selected());
        registry.register(
                UI_ATTACH,
                UiAttach.class,
                (data, ctx) -> {
                    UiAttach attach = new UiAttach();
                    attach.setDocument(data.getString("document", ""));
                    if (data.has("follow")) {
                        attach.setFollow(data.getString("follow", ""));
                    }
                    attach.setOffsetX(data.getFloat("offsetX", 0f));
                    attach.setOffsetY(data.getFloat("offsetY", 0f));
                    attach.setOffsetZ(data.getFloat("offsetZ", 0f));
                    attach.setVisible(data.getBoolean("visible", true));
                    return attach;
                });
        registry.register(
                AMBIENT_LIGHT,
                AmbientLight.class,
                (data, ctx) -> {
                    AmbientLight light = new AmbientLight();
                    light.setEnabled(data.getBoolean("enabled", true));
                    light.setIntensity(data.getFloat("intensity", 1f));
                    applyColor(data, light::setColor);
                    return light;
                });
        registry.register(
                DIRECTIONAL_LIGHT,
                DirectionalLight.class,
                (data, ctx) -> {
                    DirectionalLight light = new DirectionalLight();
                    light.setEnabled(data.getBoolean("enabled", true));
                    light.setIntensity(data.getFloat("intensity", 1f));
                    applyColor(data, light::setColor);
                    applyDirection(data, light::setDirection);
                    return light;
                });
        registry.register(
                POINT_LIGHT,
                PointLight.class,
                (data, ctx) -> {
                    PointLight light = new PointLight();
                    light.setEnabled(data.getBoolean("enabled", true));
                    light.setIntensity(data.getFloat("intensity", 1f));
                    light.setRange(data.getFloat("range", 10f));
                    applyColor(data, light::setColor);
                    return light;
                });
        registry.register(
                SPOT_LIGHT,
                SpotLight.class,
                (data, ctx) -> {
                    SpotLight light = new SpotLight();
                    light.setEnabled(data.getBoolean("enabled", true));
                    light.setIntensity(data.getFloat("intensity", 1f));
                    light.setRange(data.getFloat("range", 10f));
                    light.setCutoffAngle(data.getFloat("cutoffAngle", 45f));
                    light.setExponent(data.getFloat("exponent", 1f));
                    applyColor(data, light::setColor);
                    applyDirection(data, light::setDirection);
                    return light;
                });
        registry.register(
                AMBIENT_SOURCE,
                AmbientSource.class,
                (data, ctx) -> {
                    AmbientSource source = new AmbientSource();
                    source.setClip(data.getString("clip", ""));
                    source.setClipIsId(data.getBoolean("clipIsId", false));
                    source.setBus(parseAudioBus(data.getString("bus", "ambient"), AudioBus.AMBIENT));
                    source.setVolume(data.getFloat("volume", 1f));
                    source.setLoop(data.getBoolean("loop", true));
                    source.setMinDistance(data.getFloat("minDistance", 1f));
                    source.setMaxDistance(data.getFloat("maxDistance", 50f));
                    source.setRefDistance(data.getFloat("refDistance", 1f));
                    return source;
                });
        registry.register(
                SOUND_EMITTER,
                SoundEmitter.class,
                (data, ctx) -> {
                    SoundEmitter emitter = new SoundEmitter();
                    emitter.setClip(data.getString("clip", ""));
                    emitter.setClipIsId(data.getBoolean("clipIsId", false));
                    emitter.setBus(parseAudioBus(data.getString("bus", "sfx"), AudioBus.SFX));
                    emitter.setVolume(data.getFloat("volume", 1f));
                    emitter.setPitch(data.getFloat("pitch", 1f));
                    emitter.setLoop(data.getBoolean("loop", false));
                    emitter.setPlayOn(parsePlayOn(data.getString("playOn", "manual")));
                    emitter.setIntervalSeconds(data.getFloat("intervalSeconds", 0f));
                    return emitter;
                });
        registry.register(
                FOOTSTEP_EMITTER,
                FootstepEmitter.class,
                (data, ctx) -> {
                    FootstepEmitter emitter = new FootstepEmitter();
                    if (data instanceof JsonComponentData) {
                        emitter.setClips(((JsonComponentData) data).getStringArray("clips"));
                    }
                    emitter.setClipIsId(data.getBoolean("clipIsId", false));
                    emitter.setIntervalSeconds(data.getFloat("intervalSeconds", 0.35f));
                    emitter.setMinSpeed(data.getFloat("minSpeed", 0.5f));
                    emitter.setBus(parseAudioBus(data.getString("bus", "sfx"), AudioBus.SFX));
                    emitter.setVolume(data.getFloat("volume", 0.6f));
                    return emitter;
                });
    }

    public static void registerSystems(HermesEngine engine) {
        HermesEngineImpl impl = engine instanceof HermesEngineImpl ? (HermesEngineImpl) engine : null;
        if (engine instanceof HermesEngineImpl) {
            AudioServiceImpl audio = (AudioServiceImpl) engine.audio();
            engine.addSystem(new AmbientAudioSystem(audio), SystemScope.ACTIVE_SCENE);
            engine.addSystem(new SoundEmitterSystem(audio), SystemScope.ACTIVE_SCENE);
            engine.addSystem(new FootstepSystem(audio), SystemScope.ACTIVE_SCENE);
            engine.addSystem(new AudioActionSystem(engine.input().actions(), audio), SystemScope.GLOBAL);
        }
        engine.addSystem(new BuiltinLightingSystem(), SystemScope.ACTIVE_SCENE);
        if (impl != null) {
            engine.addSystem(
                    new AnimationSystem(impl.animationBackends(), (ResourceManagerImpl) impl.resources()),
                    SystemScope.ACTIVE_SCENE);
        }
        engine.addSystem(new SpatialIndexSystem(), SystemScope.ACTIVE_SCENE);
        engine.addSystem(new SelectionSystem(engine.input()), SystemScope.GLOBAL);
        if (engine instanceof HermesEngineImpl) {
            engine.addSystem(new CameraControlSystem((HermesEngineImpl) engine), SystemScope.GLOBAL);
        } else {
            engine.addSystem(new CameraControlSystem(engine.input()), SystemScope.GLOBAL);
        }
        engine.addSystem(new EntityDragSystem(engine.viewport(), engine.input()), SystemScope.GLOBAL);
        if (impl != null) {
            engine.addSystem(new UiInputSystem(impl), SystemScope.GLOBAL);
            engine.addSystem(new UiAttachSystem((UiServiceImpl) impl.ui(), impl.viewport()), SystemScope.GLOBAL);
        }
    }

    @FunctionalInterface
    private interface ColorSetter {
        void setColor(float r, float g, float b, float a);
    }

    @FunctionalInterface
    private interface DirectionSetter {
        void setDirection(float x, float y, float z);
    }

    private static List<DrawablePart> parseDrawableParts(JsonValue partsArray, ComponentContext ctx) {
        List<DrawablePart> parts = new ArrayList<>();
        if (partsArray == null || !partsArray.isArray()) {
            return parts;
        }
        for (JsonValue entry : partsArray) {
            parts.add(parseDrawablePart(entry, ctx));
        }
        return parts;
    }

    private static AnimationController parseAnimationController(ComponentData data, ComponentContext ctx) {
        if (!(data instanceof JsonComponentData)) {
            throw new IllegalArgumentException("AnimationController requires object JSON data");
        }
        JsonValue root = ((JsonComponentData) data).value();
        JsonValue clipsNode = root.get("clips");
        if (clipsNode == null || !clipsNode.isObject()) {
            throw new IllegalArgumentException("AnimationController.clips is required");
        }
        Map<String, AnimationClipRef> clips = new LinkedHashMap<>();
        boolean hasGltfClip = false;
        for (JsonValue entry : clipsNode) {
            AnimationClipRef clipRef = parseAnimationClipRef(entry, ctx);
            clips.put(entry.name, clipRef);
            if (clipRef.type() == AnimationClipType.GLTF) {
                hasGltfClip = true;
            }
        }
        if (clips.isEmpty()) {
            throw new IllegalArgumentException("AnimationController.clips must not be empty");
        }
        AnimationController controller = new AnimationController();
        controller.setClips(clips);
        String rigPart = root.getString("rigPart", "").trim();
        if (hasGltfClip && rigPart.isBlank()) {
            throw new IllegalArgumentException(
                    "AnimationController.rigPart is required when clips include type 'gltf'");
        }
        controller.setRigPart(rigPart);
        String defaultClip = root.getString("default", "");
        if (defaultClip == null || defaultClip.isBlank()) {
            defaultClip = clips.keySet().iterator().next();
        }
        if (!clips.containsKey(defaultClip)) {
            throw new IllegalArgumentException(
                    "AnimationController.default must reference a clip in AnimationController.clips");
        }
        controller.setDefaultClip(defaultClip);
        controller.setSpeed(root.getFloat("speed", 1f));
        controller.setAutoPlay(root.getBoolean("autoPlay", true));
        if (controller.autoPlay()) {
            controller.initPlayback();
        }
        return controller;
    }

    private static AnimationClipRef parseAnimationClipRef(JsonValue entry, ComponentContext ctx) {
        if (entry == null || entry.isNull()) {
            throw new IllegalArgumentException("AnimationController clip entry must be string or object");
        }
        if (entry.isString()) {
            String path = resolveResourcePath(entry.asString(), ctx);
            return AnimationClipRef.hermes(path);
        }
        if (!entry.isObject()) {
            throw new IllegalArgumentException("AnimationController clip entry must be string or object");
        }
        String typeValue = entry.getString("type", "").trim();
        AnimationClipType type = parseAnimationClipType(typeValue, entry);
        boolean loop = entry.getBoolean("loop", true);
        float speed = entry.getFloat("speed", 1f);
        if (type == AnimationClipType.HERMES) {
            String pathValue = entry.getString("path", entry.getString("clip", ""));
            String resolved = resolveResourcePath(pathValue, ctx);
            return AnimationClipRef.hermes(resolved).withLoop(loop).withSpeed(speed);
        }
        String clipName = entry.getString("clip", entry.getString("path", ""));
        return AnimationClipRef.gltf(clipName).withLoop(loop).withSpeed(speed);
    }

    private static AnimationClipType parseAnimationClipType(String rawType, JsonValue entry) {
        if (rawType == null || rawType.isBlank()) {
            return entry.has("path") ? AnimationClipType.HERMES : AnimationClipType.GLTF;
        }
        String normalized = rawType.trim().toLowerCase();
        if ("hermes".equals(normalized)) {
            return AnimationClipType.HERMES;
        }
        if ("gltf".equals(normalized)) {
            return AnimationClipType.GLTF;
        }
        throw new IllegalArgumentException("unknown AnimationController clip type: " + rawType);
    }

    private static DrawablePart parseDrawablePart(JsonValue entry, ComponentContext ctx) {
        String id = entry.getString("id", "default");
        DrawableKind kind = parseDrawableKind(entry);
        DrawablePart part =
                kind == DrawableKind.SPRITE
                        ? DrawablePart.sprite(id, "")
                        : DrawablePart.mesh(id, "");
        if (entry.has("texture")) {
            part.setTexture(resolveResourcePath(entry.getString("texture", ""), ctx));
        }
        if (entry.has("primitive")) {
            String primitive = entry.getString("primitive", "");
            float[] size = entry.has("size") ? toFloatArray(entry.get("size")) : null;
            part.setPrimitive(primitive);
            part.setSize(size);
            part.setModel(PrimitiveModelDocument.syntheticPath(primitive, size));
        } else if (entry.has("model")) {
            part.setModel(resolveResourcePath(entry.getString("model", ""), ctx));
        }
        if (entry.has("local")) {
            applyLocalTransform(entry.get("local"), part.local());
        }
        if (entry.has("sheet")) {
            part.setSheet(parseSpriteSheet(entry.get("sheet")));
        }
        if (entry.has("material")) {
            part.setPartMaterial(parsePartMaterial(entry.get("material")));
        }
        if (entry.has("rig")) {
            part.setRig(parseDrawableRig(entry.getString("rig", "")));
        }
        return part;
    }

    private static DrawableKind parseDrawableKind(JsonValue entry) {
        if (entry.has("kind")) {
            String kind = entry.getString("kind", "").trim().toLowerCase();
            if ("sprite".equals(kind)) {
                return DrawableKind.SPRITE;
            }
            if ("mesh".equals(kind)) {
                return DrawableKind.MESH;
            }
            throw new IllegalArgumentException("unknown Drawables part kind: " + entry.getString("kind", ""));
        }
        if (entry.has("model") || entry.has("primitive")) {
            return DrawableKind.MESH;
        }
        if (entry.has("texture")) {
            return DrawableKind.SPRITE;
        }
        return DrawableKind.MESH;
    }

    private static void applyLocalTransform(JsonValue local, LocalTransform transform) {
        if (local == null || !local.isObject()) {
            return;
        }
        transform.setX(local.getFloat("x", 0f));
        transform.setY(local.getFloat("y", 0f));
        transform.setZ(local.getFloat("z", 0f));
        transform.setRotationX(local.getFloat("rotationX", 0f));
        transform.setRotationY(local.getFloat("rotationY", 0f));
        transform.setRotationZ(local.getFloat("rotationZ", 0f));
        transform.setScaleX(local.getFloat("scaleX", 1f));
        transform.setScaleY(local.getFloat("scaleY", 1f));
        transform.setScaleZ(local.getFloat("scaleZ", 1f));
        if (local.has("visible")) {
            transform.setVisible(local.getBoolean("visible", true));
        }
        if (local.has("spriteFrame")) {
            transform.setSpriteFrame(local.getInt("spriteFrame", 0));
        }
    }

    private static SpriteSheet parseSpriteSheet(JsonValue sheet) {
        SpriteSheet result = new SpriteSheet();
        if (sheet == null || !sheet.isObject()) {
            return result;
        }
        result.setColumns(sheet.getInt("columns", 1));
        result.setRows(sheet.getInt("rows", 1));
        result.setFrameWidth(sheet.getInt("frameWidth", 1));
        result.setFrameHeight(sheet.getInt("frameHeight", 1));
        return result;
    }

    private static PartMaterial parsePartMaterial(JsonValue material) {
        PartMaterial result = new PartMaterial();
        if (material == null || !material.isObject()) {
            return result;
        }
        if (material.has("shader")) {
            result.setShader(material.getString("shader", ""));
        }
        if (material.has("uniforms") && material.get("uniforms").isObject()) {
            Map<String, MaterialUniform> uniforms = new HashMap<>();
            for (JsonValue entry : material.get("uniforms")) {
                uniforms.put(entry.name, new MaterialUniform(toFloatArray(entry)));
            }
            result.setUniforms(uniforms);
        }
        return result;
    }

    private static DrawableRig parseDrawableRig(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        if ("gltf".equals(normalized)) {
            return DrawableRig.GLTF;
        }
        throw new IllegalArgumentException("unknown Drawables part rig: " + value);
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
            return new float[]{value.asFloat()};
        }
        return new float[0];
    }

    private static String resolveResourcePath(String value, ComponentContext ctx) {
        if (value == null || value.isBlank()) {
            return value == null ? "" : value;
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("@")) {
            return trimmed;
        }
        ResourceService resources = ctx.resources();
        if (resources == null) {
            return trimmed;
        }
        return resources.resolve(trimmed).raw();
    }

    private static void applyColor(ComponentData data, ColorSetter setter) {
        if (!(data instanceof JsonComponentData)) {
            return;
        }
        JsonComponentData json = (JsonComponentData) data;
        if (!json.has("color")) {
            return;
        }
        float[] color = json.getFloatArray("color");
        if (color.length == 3) {
            setter.setColor(color[0], color[1], color[2], 1f);
        } else if (color.length == 4) {
            setter.setColor(color[0], color[1], color[2], color[3]);
        }
    }

    private static void applyDirection(ComponentData data, DirectionSetter setter) {
        if (!(data instanceof JsonComponentData)) {
            return;
        }
        JsonComponentData json = (JsonComponentData) data;
        if (!json.has("direction")) {
            return;
        }
        float[] direction = json.getFloatArray("direction");
        if (direction.length >= 3) {
            setter.setDirection(direction[0], direction[1], direction[2]);
        }
    }

    private static PickLayer parsePickLayer(String value) {
        if (value == null || value.isBlank()) {
            return PickLayer.WORLD;
        }
        String normalized = value.trim().toUpperCase();
        if (!"WORLD".equals(normalized)) {
            throw new IllegalArgumentException("unknown Selectable.layer: " + value);
        }
        return PickLayer.WORLD;
    }

    private static RenderLayer.Layer parseRenderLayer(String value) {
        if (value == null || value.isBlank()) {
            return RenderLayer.Layer.WORLD;
        }
        String normalized = value.trim().toUpperCase();
        if (!"WORLD".equals(normalized)) {
            throw new IllegalArgumentException("unknown RenderLayer.layer: " + value);
        }
        return RenderLayer.Layer.WORLD;
    }

    private static Camera.Projection parseProjection(String value) {
        if (value == null) {
            return Camera.Projection.ORTHOGRAPHIC;
        }
        String normalized = value.trim().toLowerCase();
        if ("perspective".equals(normalized) || "3d".equals(normalized)) {
            return Camera.Projection.PERSPECTIVE;
        }
        return Camera.Projection.ORTHOGRAPHIC;
    }

    private static AudioBus parseAudioBus(String value, AudioBus defaultBus) {
        if (value == null || value.isBlank()) {
            return defaultBus;
        }
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "sfx":
                return AudioBus.SFX;
            case "ambient":
                return AudioBus.AMBIENT;
            case "music":
                return AudioBus.MUSIC;
            default:
                throw new IllegalArgumentException("unknown audio bus: " + value);
        }
    }

    private static SoundEmitter.PlayOn parsePlayOn(String value) {
        if (value == null || value.isBlank()) {
            return SoundEmitter.PlayOn.MANUAL;
        }
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "spawn":
                return SoundEmitter.PlayOn.SPAWN;
            case "interval":
                return SoundEmitter.PlayOn.INTERVAL;
            case "manual":
                return SoundEmitter.PlayOn.MANUAL;
            default:
                throw new IllegalArgumentException("unknown SoundEmitter.playOn: " + value);
        }
    }

    private static dev.hermes.api.ecs.ViewportFitMode parseFitMode(String value) {
        if (value == null) {
            return dev.hermes.api.ecs.ViewportFitMode.LETTERBOX;
        }
        String normalized = value.trim().toLowerCase();
        if ("stretch".equals(normalized)) {
            return dev.hermes.api.ecs.ViewportFitMode.STRETCH;
        }
        if ("crop".equals(normalized)) {
            return dev.hermes.api.ecs.ViewportFitMode.CROP;
        }
        if ("fixed".equals(normalized)) {
            return dev.hermes.api.ecs.ViewportFitMode.FIXED;
        }
        return dev.hermes.api.ecs.ViewportFitMode.LETTERBOX;
    }
}
