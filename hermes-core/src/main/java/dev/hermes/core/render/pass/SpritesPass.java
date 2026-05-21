package dev.hermes.core.render.pass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.SceneCamera;
import dev.hermes.core.ecs.SpriteDrawOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Renders sprites in world space using the active scene camera projection. */
public final class SpritesPass {

  private final SpriteBatch batch;
  private final Map<String, TextureRegion> regions = new HashMap<>();
  private final SceneCamera sceneCamera = new SceneCamera();
  private final Vector3 projected = new Vector3();
  private float windowWidth = 640f;
  private float windowHeight = 480f;

  public SpritesPass(SpriteBatch batch) {
    this.batch = batch;
    sceneCamera.resize(windowWidth, windowHeight);
  }

  public void resize(int width, int height) {
    windowWidth = Math.max(1, width);
    windowHeight = Math.max(1, height);
    sceneCamera.resize(windowWidth, windowHeight);
  }

  public void render(World world) {
    syncWindowSize();
    ActiveCamera active = CameraResolver.resolve(world, windowWidth, windowHeight);
    sceneCamera.apply(active);

    List<Entity> drawables = collectDrawableEntities(world);
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
    float width = region.getRegionWidth() * transform.scaleX();
    float height = region.getRegionHeight() * transform.scaleY();
    float originX = width * 0.5f;
    float originY = height * 0.5f;
    float x = transform.x();
    float y = transform.y();
    float rotation = transform.rotationZ();

    if (active.projection() == Camera.Projection.PERSPECTIVE) {
      projected.set(x, y, transform.z());
      sceneCamera.gdxCamera().project(projected);
      batch.draw(region, projected.x, projected.y, originX, originY, width, height, 1f, 1f, rotation);
    } else {
      batch.draw(region, x, y, originX, originY, width, height, 1f, 1f, rotation);
    }
  }

  private void syncWindowSize() {
    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();
    if (width > 0 && height > 0 && (width != (int) windowWidth || height != (int) windowHeight)) {
      resize(width, height);
    }
  }

  private static List<Entity> collectDrawableEntities(World world) {
    List<Entity> result = new ArrayList<>();
    for (Entity entity : world.entitiesWith(Sprite.class)) {
      if (world.hasComponent(entity.id(), Camera.class)) {
        continue;
      }
      if (world.hasComponent(entity.id(), Transform.class)) {
        result.add(entity);
      }
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
