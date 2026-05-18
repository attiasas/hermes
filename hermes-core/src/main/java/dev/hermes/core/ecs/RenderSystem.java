package dev.hermes.core.ecs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import java.util.HashMap;
import java.util.Map;

/** Renders entities that have both {@link Transform} and {@link Sprite}. */
public final class RenderSystem implements System {

  private final SpriteBatch batch;
  private final Map<String, Texture> textures = new HashMap<>();

  public RenderSystem(SpriteBatch batch) {
    this.batch = batch;
  }

  @Override
  public void render(World world) {
    batch.begin();
    for (Entity entity : world.entitiesWith(Transform.class)) {
      Transform transform = world.getComponent(entity.id(), Transform.class);
      Sprite sprite = world.getComponent(entity.id(), Sprite.class);
      if (transform == null || sprite == null) {
        continue;
      }
      String texturePath = sprite.texture();
      if (texturePath == null || texturePath.isBlank()) {
        continue;
      }
      Texture texture = textures.computeIfAbsent(texturePath, Texture::new);
      batch.draw(texture, transform.x(), transform.y());
    }
    batch.end();
  }

  public void dispose() {
    for (Texture texture : textures.values()) {
      texture.dispose();
    }
    textures.clear();
  }
}
