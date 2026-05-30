package dev.hermes.core.ecs;

import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.HermesEngine;
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
import dev.hermes.core.input.CameraSceneControlSystem;
import dev.hermes.core.input.EntityDragSystem;
import dev.hermes.core.input.SelectionSystem;
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
                    sprite.setTexture(data.getString("texture", ""));
                    return sprite;
                });
        registry.register(
                CAMERA,
                Camera.class,
                (data, ctx) -> {
                    Camera camera = new Camera();
                    camera.setProjection(parseProjection(data.getString("projection", "orthographic")));
                    camera.setActive(data.getBoolean("active", true));
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
                    mesh.setModel(data.getString("model", ""));
                    if (data.has("texture")) {
                        mesh.setTexture(data.getString("texture", ""));
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
    }

    public static void registerSystems(HermesEngine engine) {
        engine.addSystem(new SelectionSystem(engine.input()), SystemScope.GLOBAL);
        engine.addSystem(new CameraSceneControlSystem(engine.input()), SystemScope.GLOBAL);
        engine.addSystem(new EntityDragSystem(engine.viewport(), engine.input()), SystemScope.GLOBAL);
        if (engine instanceof HermesEngineImpl) {
            HermesEngineImpl impl = (HermesEngineImpl) engine;
            engine.addSystem(new UiInputSystem(impl), SystemScope.GLOBAL);
            engine.addSystem(new UiAttachSystem((UiServiceImpl) impl.ui(), impl.viewport()), SystemScope.GLOBAL);
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
