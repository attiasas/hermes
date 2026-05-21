package dev.hermes.core.ecs;

import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;

final class BuiltinComponents {

  static final String TRANSFORM = "Transform";
  static final String SPRITE = "Sprite";
  static final String CAMERA = "Camera";

  private BuiltinComponents() {}

  static void register(ComponentRegistry registry) {
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
        },
        BuiltinComponentSerializers.transform(),
        BuiltinComponentSerializers.transformDescriptor());
    registry.register(
        SPRITE,
        Sprite.class,
        data -> {
          Sprite sprite = new Sprite();
          sprite.setTexture(data.getString("texture", ""));
          return sprite;
        },
        BuiltinComponentSerializers.sprite(),
        BuiltinComponentSerializers.spriteDescriptor());
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
          return camera;
        },
        BuiltinComponentSerializers.camera(),
        BuiltinComponentSerializers.cameraDescriptor());
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
}
