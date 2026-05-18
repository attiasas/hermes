package dev.hermes.core.ecs;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;

final class BuiltinComponents {

  static final String TRANSFORM = "Transform";
  static final String SPRITE = "Sprite";

  private BuiltinComponents() {}

  static void register(ComponentRegistry registry) {
    registry.register(
        TRANSFORM,
        Transform.class,
        data -> {
          Transform transform = new Transform();
          transform.setX(data.getFloat("x", 0f));
          transform.setY(data.getFloat("y", 0f));
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
  }
}
