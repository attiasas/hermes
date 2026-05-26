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
import dev.hermes.api.input.PickLayer;
import dev.hermes.core.input.SelectionSystem;

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

    private BuiltinComponents() {
    }

    public static void register(ComponentRegistry registry) {
        registry.register(
                TRANSFORM,
                Transform.class,
                data -> {
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
                data -> {
                    Sprite sprite = new Sprite();
                    sprite.setTexture(data.getString("texture", ""));
                    return sprite;
                });
        registry.register(
                CAMERA,
                Camera.class,
                data -> {
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
                data -> {
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
                data -> {
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
                data -> {
                    RenderLayer renderLayer = new RenderLayer();
                    renderLayer.setLayer(parseRenderLayer(data.getString("layer", "WORLD")));
                    return renderLayer;
                });
        registry.register(
                SELECTABLE,
                Selectable.class,
                data -> {
                    Selectable selectable = new Selectable();
                    selectable.setEnabled(data.getBoolean("enabled", true));
                    selectable.setRadius(data.getFloat("radius", 16f));
                    selectable.setLayer(parsePickLayer(data.getString("layer", "WORLD")));
                    return selectable;
                });
        registry.register(SELECTED, Selected.class, data -> new Selected());
    }

    public static void registerSystems(HermesEngine engine) {
        engine.addSystem(new SelectionSystem(engine.input()), SystemScope.GLOBAL);
    }

    private static PickLayer parsePickLayer(String value) {
        if (value == null) {
            return PickLayer.WORLD;
        }
        String normalized = value.trim().toUpperCase();
        if ("UI".equals(normalized)) {
            return PickLayer.UI;
        }
        return PickLayer.WORLD;
    }

    private static RenderLayer.Layer parseRenderLayer(String value) {
        if (value == null) {
            return RenderLayer.Layer.WORLD;
        }
        String normalized = value.trim().toUpperCase();
        if ("UI".equals(normalized)) {
            return RenderLayer.Layer.UI;
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
