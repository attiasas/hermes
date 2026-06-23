package dev.hermes.core.render.pass;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.DrawableKind;
import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.lighting.LightingRuntime;
import dev.hermes.core.render.ShaderCompileException;
import dev.hermes.core.render.resource.MaterialUniformBinder;
import dev.hermes.core.render.resource.ShaderRegistry;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.viewport.BoundCamera;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Renders mesh entities in entities space with the active scene camera.
 */
public final class World3dPass {

    private final ResourceManagerImpl resources;
    private final ShaderRegistry shaderRegistry;
    private final ModelBatch modelBatch;
    private final Matrix4 instanceTransform = new Matrix4();
    private final Map<String, Shader> g3dShaderCache = new HashMap<>();
    private final RenderablePool renderablePool = new RenderablePool();
    private final Array<Renderable> renderableScratch = new Array<>(1);

    public World3dPass(ResourceManagerImpl resources) {
        this(resources, null);
    }

    public World3dPass(ResourceManagerImpl resources, ShaderRegistry shaderRegistry) {
        this.resources = Objects.requireNonNull(resources, "resources");
        this.shaderRegistry = shaderRegistry;
        this.modelBatch = new ModelBatch();
    }

    public void resize(int width, int height) {
    }

    public void render(EntityStore entities) {
        throw new UnsupportedOperationException("Use render(EntityStore, layers, BoundCamera)");
    }

    public void render(EntityStore entities, Set<RenderLayer.Layer> layers, BoundCamera bound) {
        List<Entity> drawables = collectDrawables(entities, layers);
        if (drawables.isEmpty()) {
            return;
        }

        Environment environment = LightingRuntime.require(entities);
        modelBatch.begin(bound.gdxCamera());
        for (Entity entity : drawables) {
            drawMesh(entities, entity, environment);
        }
        modelBatch.end();
    }

    private void drawMesh(EntityStore entities, Entity entity, Environment environment) {
        Transform transform = entities.getComponent(entity.id(), Transform.class);
        Drawables drawables = entities.getComponent(entity.id(), Drawables.class);
        Material material = entities.getComponent(entity.id(), Material.class);
        DrawablePart meshPart = firstMeshPart(drawables);
        if (transform == null || meshPart == null || material == null) {
            return;
        }
        String modelPath = meshPart.model();
        if (modelPath == null || modelPath.isBlank()) {
            return;
        }

        String shaderId = material.shader();
        if (shaderRegistry != null && !shaderRegistry.isRegistered(shaderId)) {
            throw new ShaderCompileException("shader not registered: " + shaderId);
        }

        ResourceRef ref = ResourceRef.of(modelPath);
        resources.loadSync(ref, ResourceKind.MODEL);
        ModelInstance instance = new ModelInstance(ResourceAccess.model(resources, ref));
        applyTransform(instance.transform, transform);

        Shader g3dShader = resolveG3dShader(shaderId, instance, environment);
        if (g3dShader != null) {
            applyMaterialUniforms(g3dShader, material);
            modelBatch.render(instance, environment, g3dShader);
        } else {
            modelBatch.render(instance, environment);
        }
    }

    private Shader resolveG3dShader(String shaderId, ModelInstance instance, Environment environment) {
        if (shaderRegistry == null || shaderRegistry.usesBuiltin(shaderId)) {
            return null;
        }
        return g3dShaderCache.computeIfAbsent(
                shaderId,
                id -> {
                    Renderable renderable = firstRenderable(instance);
                    return shaderRegistry.resolveG3dShader(id, renderable, environment);
                });
    }

    private Renderable firstRenderable(ModelInstance instance) {
        renderableScratch.clear();
        instance.getRenderables(renderableScratch, renderablePool);
        if (renderableScratch.size == 0) {
            throw new IllegalStateException("model has no renderables");
        }
        return renderableScratch.first();
    }

    private static void applyMaterialUniforms(Shader g3dShader, Material material) {
        if (g3dShader instanceof DefaultShader) {
            MaterialUniformBinder.apply(((DefaultShader) g3dShader).program, material);
        }
    }

    private static void applyTransform(Matrix4 matrix, Transform transform) {
        matrix.idt();
        matrix.translate(transform.x(), transform.y(), transform.z());
        if (transform.rotationX() != 0f) {
            matrix.rotate(Vector3.X, transform.rotationX());
        }
        if (transform.rotationY() != 0f) {
            matrix.rotate(Vector3.Y, transform.rotationY());
        }
        if (transform.rotationZ() != 0f) {
            matrix.rotate(Vector3.Z, transform.rotationZ());
        }
        matrix.scale(transform.scaleX(), transform.scaleY(), transform.scaleZ());
    }

    /**
     * Returns mesh entities that have transform and material (excludes camera entities).
     */
    public static List<Entity> collectDrawables(EntityStore entities) {
        return collectDrawables(entities, EnumSet.of(RenderLayer.Layer.WORLD));
    }

    public static List<Entity> collectDrawables(EntityStore entities, Set<RenderLayer.Layer> layers) {
        Set<RenderLayer.Layer> allowed =
                layers == null || layers.isEmpty() ? EnumSet.of(RenderLayer.Layer.WORLD) : layers;
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities.entitiesWith(Drawables.class)) {
            if (entities.hasComponent(entity.id(), Camera.class)) {
                continue;
            }
            if (!entities.hasComponent(entity.id(), Transform.class)) {
                continue;
            }
            if (!entities.hasComponent(entity.id(), Material.class)) {
                continue;
            }
            Drawables drawables = entities.getComponent(entity.id(), Drawables.class);
            if (firstMeshPart(drawables) == null) {
                continue;
            }
            RenderLayer renderLayer = entities.getComponent(entity.id(), RenderLayer.class);
            RenderLayer.Layer layer =
                    renderLayer == null ? RenderLayer.Layer.WORLD : renderLayer.layer();
            if (!allowed.contains(layer)) {
                continue;
            }
            result.add(entity);
        }
        return result;
    }

    private static DrawablePart firstMeshPart(Drawables drawables) {
        if (drawables == null) {
            return null;
        }
        for (DrawablePart part : drawables.parts()) {
            if (part.kind() == DrawableKind.MESH) {
                return part;
            }
        }
        return null;
    }

    public void dispose() {
        for (Shader shader : g3dShaderCache.values()) {
            shader.dispose();
        }
        g3dShaderCache.clear();
        modelBatch.dispose();
    }

    private static final class RenderablePool extends FlushablePool<Renderable> {
        @Override
        protected Renderable newObject() {
            return new Renderable();
        }
    }
}
