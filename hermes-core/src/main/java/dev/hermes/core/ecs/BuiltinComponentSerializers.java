package dev.hermes.core.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.ComponentInspectorDescriptor;
import dev.hermes.api.ecs.ComponentSerializer;
import dev.hermes.api.ecs.FieldDescriptor;
import dev.hermes.api.ecs.FieldKind;
import dev.hermes.api.ecs.MutableComponentData;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import java.util.List;

final class BuiltinComponentSerializers {

  private static final List<String> PROJECTION_VALUES = List.of("orthographic", "perspective");

  private BuiltinComponentSerializers() {}

  static ComponentSerializer transform() {
    return new ComponentSerializer() {
      @Override
      public void serialize(Component component, MutableComponentData out) {
        Transform t = (Transform) component;
        out.putFloat("x", t.x());
        out.putFloat("y", t.y());
        out.putFloat("z", t.z());
        out.putFloat("rotationX", t.rotationX());
        out.putFloat("rotationY", t.rotationY());
        out.putFloat("rotationZ", t.rotationZ());
        out.putFloat("scaleX", t.scaleX());
        out.putFloat("scaleY", t.scaleY());
        out.putFloat("scaleZ", t.scaleZ());
      }

      @Override
      public void applyField(Component component, String fieldName, Object value) {
        Transform t = (Transform) component;
        switch (fieldName) {
          case "x":
            t.setX(asFloat(value));
            break;
          case "y":
            t.setY(asFloat(value));
            break;
          case "z":
            t.setZ(asFloat(value));
            break;
          case "rotationX":
            t.setRotationX(asFloat(value));
            break;
          case "rotationY":
            t.setRotationY(asFloat(value));
            break;
          case "rotationZ":
            t.setRotationZ(asFloat(value));
            break;
          case "scaleX":
            t.setScaleX(asFloat(value));
            break;
          case "scaleY":
            t.setScaleY(asFloat(value));
            break;
          case "scaleZ":
            t.setScaleZ(asFloat(value));
            break;
          default:
            throw new IllegalArgumentException("Unknown Transform field: " + fieldName);
        }
      }
    };
  }

  static ComponentInspectorDescriptor transformDescriptor() {
    return () ->
        List.of(
            new FieldDescriptor("x", FieldKind.FLOAT, true),
            new FieldDescriptor("y", FieldKind.FLOAT, true),
            new FieldDescriptor("z", FieldKind.FLOAT, true),
            new FieldDescriptor("rotationX", FieldKind.FLOAT, true),
            new FieldDescriptor("rotationY", FieldKind.FLOAT, true),
            new FieldDescriptor("rotationZ", FieldKind.FLOAT, true),
            new FieldDescriptor("scaleX", FieldKind.FLOAT, true),
            new FieldDescriptor("scaleY", FieldKind.FLOAT, true),
            new FieldDescriptor("scaleZ", FieldKind.FLOAT, true));
  }

  static ComponentSerializer sprite() {
    return new ComponentSerializer() {
      @Override
      public void serialize(Component component, MutableComponentData out) {
        Sprite s = (Sprite) component;
        out.putString("texture", s.texture() == null ? "" : s.texture());
      }

      @Override
      public void applyField(Component component, String fieldName, Object value) {
        if (!"texture".equals(fieldName)) {
          throw new IllegalArgumentException("Unknown Sprite field: " + fieldName);
        }
        ((Sprite) component).setTexture(value == null ? "" : value.toString());
      }
    };
  }

  static ComponentInspectorDescriptor spriteDescriptor() {
    return () -> List.of(new FieldDescriptor("texture", FieldKind.STRING, true));
  }

  static ComponentSerializer camera() {
    return new ComponentSerializer() {
      @Override
      public void serialize(Component component, MutableComponentData out) {
        Camera c = (Camera) component;
        out.putString("projection", projectionToString(c.projection()));
        out.putBoolean("active", c.active());
        out.putFloat("zoom", c.zoom());
        out.putFloat("fieldOfView", c.fieldOfView());
        out.putFloat("near", c.near());
        out.putFloat("far", c.far());
        out.putFloat("viewportWidth", c.viewportWidth());
        out.putFloat("viewportHeight", c.viewportHeight());
      }

      @Override
      public void applyField(Component component, String fieldName, Object value) {
        Camera c = (Camera) component;
        switch (fieldName) {
          case "projection":
            c.setProjection(parseProjection(value == null ? null : value.toString()));
            break;
          case "active":
            c.setActive(asBoolean(value));
            break;
          case "zoom":
            c.setZoom(asFloat(value));
            break;
          case "fieldOfView":
            c.setFieldOfView(asFloat(value));
            break;
          case "near":
            c.setNear(asFloat(value));
            break;
          case "far":
            c.setFar(asFloat(value));
            break;
          case "viewportWidth":
            c.setViewportWidth(asFloat(value));
            break;
          case "viewportHeight":
            c.setViewportHeight(asFloat(value));
            break;
          default:
            throw new IllegalArgumentException("Unknown Camera field: " + fieldName);
        }
      }
    };
  }

  static ComponentInspectorDescriptor cameraDescriptor() {
    return () ->
        List.of(
            new FieldDescriptor(
                "projection",
                FieldKind.ENUM,
                true,
                null,
                null,
                null,
                PROJECTION_VALUES),
            new FieldDescriptor("active", FieldKind.BOOLEAN, true),
            new FieldDescriptor("zoom", FieldKind.FLOAT, true),
            new FieldDescriptor("fieldOfView", FieldKind.FLOAT, true),
            new FieldDescriptor("near", FieldKind.FLOAT, true),
            new FieldDescriptor("far", FieldKind.FLOAT, true),
            new FieldDescriptor("viewportWidth", FieldKind.FLOAT, true),
            new FieldDescriptor("viewportHeight", FieldKind.FLOAT, true));
  }

  private static float asFloat(Object value) {
    if (value instanceof Number) {
      return ((Number) value).floatValue();
    }
    return Float.parseFloat(value.toString());
  }

  private static boolean asBoolean(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue() != 0;
    }
    return Boolean.parseBoolean(value.toString());
  }

  private static String projectionToString(Camera.Projection projection) {
    return projection == Camera.Projection.PERSPECTIVE ? "perspective" : "orthographic";
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
