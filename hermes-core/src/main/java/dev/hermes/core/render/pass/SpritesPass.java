package dev.hermes.core.render.pass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.SceneCamera;
import dev.hermes.core.ecs.SpriteDrawOrder;
import dev.hermes.core.render.ShaderCompileException;
import dev.hermes.core.render.resource.MaterialUniformBinder;
import dev.hermes.core.render.resource.ShaderRegistry;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Renders sprites in world space using the active scene camera projection. */
public final class SpritesPass {

  private final SpriteBatch batch;
  private final ShaderRegistry shaderRegistry;
  private final Map<String, TextureRegion> regions = new HashMap<>();
  private final SceneCamera sceneCamera = new SceneCamera();
  private final Vector3 projected = new Vector3();
  private float windowWidth = 640f;
  private float windowHeight = 480f;

  public SpritesPass(SpriteBatch batch) {
    this(batch, null);
  }

  public SpritesPass(SpriteBatch batch, ShaderRegistry shaderRegistry) {
    this.batch = batch;
    this.shaderRegistry = shaderRegistry;
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
    syncWindowSize();
    ActiveCamera active = CameraResolver.resolve(world, windowWidth, windowHeight);
    sceneCamera.apply(active);

    List<Entity> drawables = collectDrawableEntities(world, layers);
    SpriteDrawOrder.sort(drawables, world, active);

    batch.setProjectionMatrix(sceneCamera.combined());
    batch.begin();
    for (Entity entity : drawables) {
      drawSprite(world, entity, active, sceneCamera);
    }
    batch.end();
  }

  private void drawSprite(World world, Entity entity, ActiveCamera active, SceneCamera sceneCamera) {
    Transform transform = world.getComponent(entity.id(), Transform.class);
    Sprite sprite = world.getComponent(entity.id(), Sprite.class);
    if (transform == null || sprite == null) {
      return;
    }
    String texturePath = sprite.texture();
    if (texturePath == null || texturePath.isBlank()) {
      return;
    }
    TextureRegion region =
        regions.computeIfAbsent(
            texturePath,
            path -> {
              FileHandle file = HermesAssetPaths.internal(path);
              return new TextureRegion(new Texture(file));
            });

    ShaderProgram previousShader = batch.getShader();
    Material material = world.getComponent(entity.id(), Material.class);
    ShaderProgram materialShader = resolveSpriteShader(material);
    if (materialShader != null) {
      batch.setShader(materialShader);
      MaterialUniformBinder.apply(materialShader, material);
    }

    float width = region.getRegionWidth() * transform.scaleX();
    float height = region.getRegionHeight() * transform.scaleY();
    float originX = width * 0.5f;
    float originY = height * 0.5f;
    float x = transform.x();
    float y = transform.y();
    float rotation = transform.rotationZ();

    try {
      if (active.projection() == Camera.Projection.PERSPECTIVE) {
        projected.set(x, y, transform.z());
        sceneCamera.gdxCamera().project(projected);
        batch.draw(
            region, projected.x, projected.y, originX, originY, width, height, 1f, 1f, rotation);
      } else {
        batch.draw(region, x, y, originX, originY, width, height, 1f, 1f, rotation);
      }
    } finally {
      if (materialShader != null) {
        batch.setShader(previousShader);
      }
    }
  }

  private ShaderProgram resolveSpriteShader(Material material) {
    if (shaderRegistry == null || material == null) {
      return null;
    }
    String shaderId = material.shader();
    if (!shaderRegistry.isRegistered(shaderId)) {
      throw new ShaderCompileException("shader not registered: " + shaderId);
    }
    if (shaderRegistry.usesBuiltin(shaderId)) {
      return null;
    }
    return shaderRegistry.requireProgram(shaderId);
  }

  private void syncWindowSize() {
    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();
    if (width > 0 && height > 0 && (width != (int) windowWidth || height != (int) windowHeight)) {
      resize(width, height);
    }
  }

  private static List<Entity> collectDrawableEntities(World world, Set<RenderLayer.Layer> layers) {
    Set<RenderLayer.Layer> allowed =
        layers == null || layers.isEmpty() ? EnumSet.of(RenderLayer.Layer.WORLD) : layers;
    List<Entity> result = new ArrayList<>();
    for (Entity entity : world.entitiesWith(Sprite.class)) {
      if (world.hasComponent(entity.id(), Camera.class)) {
        continue;
      }
      if (!world.hasComponent(entity.id(), Transform.class)) {
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
    for (TextureRegion region : regions.values()) {
      region.getTexture().dispose();
    }
    regions.clear();
  }
}
