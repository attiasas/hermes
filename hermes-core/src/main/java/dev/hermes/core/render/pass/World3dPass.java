package dev.hermes.core.render.pass;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.Mesh;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.SceneCamera;
import dev.hermes.core.render.resource.ModelCache;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Renders mesh entities in world space with the active scene camera. */
public final class World3dPass {

  private final ModelCache modelCache;
  private final boolean disposeModelCache;
  private final ModelBatch modelBatch;
  private final SceneCamera sceneCamera = new SceneCamera();
  private final Environment environment = new Environment();
  private final Matrix4 instanceTransform = new Matrix4();
  private float windowWidth = 640f;
  private float windowHeight = 480f;

  public World3dPass(ModelCache modelCache) {
    this(modelCache, true);
  }

  public World3dPass(ModelCache modelCache, boolean disposeModelCache) {
    this.modelCache = modelCache;
    this.disposeModelCache = disposeModelCache;
    this.modelBatch = new ModelBatch();
    environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
    environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    sceneCamera.resize(windowWidth, windowHeight);
  }

  public void resize(int width, int height) {
    windowWidth = Math.max(1, width);
    windowHeight = Math.max(1, height);
    sceneCamera.resize(windowWidth, windowHeight);
  }

  public void render(World world) {
    render(world, EnumSet.of(RenderLayer.Layer.WORLD));
  }

  public void render(World world, Set<RenderLayer.Layer> layers) {
    ActiveCamera active = CameraResolver.resolve(world, windowWidth, windowHeight);
    sceneCamera.apply(active);

    List<Entity> drawables = collectDrawables(world, layers);
    if (drawables.isEmpty()) {
      return;
    }

    modelBatch.begin(sceneCamera.gdxCamera());
    for (Entity entity : drawables) {
      drawMesh(world, entity);
    }
    modelBatch.end();
  }

  private void drawMesh(World world, Entity entity) {
    Transform transform = world.getComponent(entity.id(), Transform.class);
    Mesh mesh = world.getComponent(entity.id(), Mesh.class);
    Material material = world.getComponent(entity.id(), Material.class);
    if (transform == null || mesh == null || material == null) {
      return;
    }
    String modelPath = mesh.model();
    if (modelPath == null || modelPath.isBlank()) {
      return;
    }

    ModelInstance instance = new ModelInstance(modelCache.get(modelPath));
    applyTransform(instance.transform, transform);
    modelBatch.render(instance, environment);
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

  /** Returns mesh entities that have transform and material (excludes camera entities). */
  public static List<Entity> collectDrawables(World world) {
    return collectDrawables(world, EnumSet.of(RenderLayer.Layer.WORLD));
  }

  public static List<Entity> collectDrawables(World world, Set<RenderLayer.Layer> layers) {
    Set<RenderLayer.Layer> allowed =
        layers == null || layers.isEmpty() ? EnumSet.of(RenderLayer.Layer.WORLD) : layers;
    List<Entity> result = new ArrayList<>();
    for (Entity entity : world.entitiesWith(Mesh.class)) {
      if (world.hasComponent(entity.id(), Camera.class)) {
        continue;
      }
      if (!world.hasComponent(entity.id(), Transform.class)) {
        continue;
      }
      if (!world.hasComponent(entity.id(), Material.class)) {
        continue;
      }
      RenderLayer renderLayer = world.getComponent(entity.id(), RenderLayer.class);
      RenderLayer.Layer layer =
          renderLayer == null ? RenderLayer.Layer.WORLD : renderLayer.layer();
      if (!allowed.contains(layer)) {
        continue;
      }
      result.add(entity);
    }
    return result;
  }

  public void dispose() {
    modelBatch.dispose();
    if (disposeModelCache) {
      modelCache.dispose();
    }
  }
}
