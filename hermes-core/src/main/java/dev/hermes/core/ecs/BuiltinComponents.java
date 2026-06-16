package dev.hermes.core.ecs;

import dev.hermes.api.ecs.AmbientLight;
import dev.hermes.api.ecs.AmbientSource;
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
import dev.hermes.api.ecs.Mesh;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Selectable;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.ecs.Sprite;
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
import dev.hermes.core.input.CameraSceneControlSystem;
import dev.hermes.core.input.EntityDragSystem;
import dev.hermes.core.input.SelectionSystem;
import dev.hermes.core.lighting.BuiltinLightingSystem;
import dev.hermes.core.ui.UiAttachSystem;
import dev.hermes.core.ui.UiInputSystem;
import dev.hermes.core.ui.UiServiceImpl;

import java.util.HashMap;
import java.util.Map;

public final class BuiltinComponents {

    static final String TRANSFORM = "Transform";
    static final String SPRITE = "Sprite";
    static final String CAMERA = "Camera";
    static final String MESH = "Mesh";
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
                SPRITE,
                Sprite.class,
                (data, ctx) -> {
                    Sprite sprite = new Sprite();
                    sprite.setTexture(resolveResourcePath(data.getString("texture", ""), ctx));
                    return sprite;
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
                MESH,
                Mesh.class,
                (data, ctx) -> {
                    Mesh mesh = new Mesh();
                    mesh.setModel(resolveResourcePath(data.getString("model", ""), ctx));
                    if (data.has("texture")) {
                        mesh.setTexture(resolveResourcePath(data.getString("texture", ""), ctx));
                    }
                    return mesh;
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
        if (engine instanceof HermesEngineImpl) {
            AudioServiceImpl audio = (AudioServiceImpl) engine.audio();
            engine.addSystem(new AmbientAudioSystem(audio), SystemScope.ACTIVE_SCENE);
            engine.addSystem(new SoundEmitterSystem(audio), SystemScope.ACTIVE_SCENE);
            engine.addSystem(new FootstepSystem(audio), SystemScope.ACTIVE_SCENE);
            engine.addSystem(new AudioActionSystem(engine.input().actions(), audio), SystemScope.GLOBAL);
        }
        engine.addSystem(new BuiltinLightingSystem(), SystemScope.ACTIVE_SCENE);
        engine.addSystem(new SpatialIndexSystem(), SystemScope.ACTIVE_SCENE);
        engine.addSystem(new SelectionSystem(engine.input()), SystemScope.GLOBAL);
        engine.addSystem(new CameraSceneControlSystem(engine.input()), SystemScope.GLOBAL);
        engine.addSystem(new EntityDragSystem(engine.viewport(), engine.input()), SystemScope.GLOBAL);
        if (engine instanceof HermesEngineImpl) {
            HermesEngineImpl impl = (HermesEngineImpl) engine;
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
