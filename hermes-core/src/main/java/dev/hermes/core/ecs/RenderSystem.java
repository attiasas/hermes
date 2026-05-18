package dev.hermes.core.ecs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Renders entities that have both {@link Transform} and {@link Sprite}. */
public final class RenderSystem implements System {

  private final SpriteBatch batch;
  private final Map<String, TextureRegion> regions = new HashMap<>();

  public RenderSystem(SpriteBatch batch) {
    this.batch = batch;
  }

  @Override
  public void render(World world) {
    List<Entity> drawOrder = new ArrayList<>(world.entitiesWith(Transform.class));
    drawOrder.sort(Comparator.comparingDouble(e -> world.getComponent(e.id(), Transform.class).z()));

    batch.begin();
    for (Entity entity : drawOrder) {
      Transform transform = world.getComponent(entity.id(), Transform.class);
      Sprite sprite = world.getComponent(entity.id(), Sprite.class);
      if (transform == null || sprite == null) {
        continue;
      }
      String texturePath = sprite.texture();
      if (texturePath == null || texturePath.isBlank()) {
        continue;
      }
      TextureRegion region = regions.computeIfAbsent(texturePath, path -> new TextureRegion(new Texture(path)));
      float width = region.getRegionWidth() * transform.scaleX();
      float height = region.getRegionHeight() * transform.scaleY();
      batch.draw(
          region,
          transform.screenX(),
          transform.screenY(),
          width * 0.5f,
          height * 0.5f,
          width,
          height,
          1f,
          1f,
          transform.rotationZ());
    }
    batch.end();
  }

  public void dispose() {
    for (TextureRegion region : regions.values()) {
      region.getTexture().dispose();
    }
    regions.clear();
  }
}
