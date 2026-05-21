package dev.hermes.sample;

import dev.hermes.api.Component;
import dev.hermes.api.ecs.ComponentInspectorDescriptor;
import dev.hermes.api.ecs.ComponentRegistration;
import dev.hermes.api.ecs.ComponentSerializer;
import dev.hermes.api.ecs.FieldDescriptor;
import dev.hermes.api.ecs.FieldKind;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.MutableComponentData;
import java.util.List;

/** ServiceLoader entry: registers {@link SpinMarker} and its update system (SPI path only). */
public final class SpinMarkerRegistration implements ComponentRegistration {

  @Override
  public void register(HermesEngine engine) {
    engine
        .registry()
        .register(
            "SpinMarker",
            SpinMarker.class,
            data -> {
              SpinMarker spin = new SpinMarker();
              spin.setSpeedRadiansPerSecond(data.getFloat("speed", 1f));
              spin.setCenterX(data.getFloat("centerX", 320f));
              spin.setCenterY(data.getFloat("centerY", 240f));
              spin.setRadius(data.getFloat("radius", 100f));
              spin.setAngleRadians(data.getFloat("angle", 0f));
              return spin;
            },
            spinMarkerSerializer(),
            spinMarkerDescriptor());
    engine.addSystem(new SpinMarkerSystem());
  }

  private static ComponentSerializer spinMarkerSerializer() {
    return new ComponentSerializer() {
      @Override
      public void serialize(Component component, MutableComponentData out) {
        SpinMarker spin = (SpinMarker) component;
        out.putFloat("speedRadiansPerSecond", spin.speedRadiansPerSecond());
        out.putFloat("centerX", spin.centerX());
        out.putFloat("centerY", spin.centerY());
        out.putFloat("radius", spin.radius());
        out.putFloat("angleRadians", spin.angleRadians());
      }

      @Override
      public void applyField(Component component, String fieldName, Object value) {
        SpinMarker spin = (SpinMarker) component;
        switch (fieldName) {
          case "speedRadiansPerSecond":
            spin.setSpeedRadiansPerSecond(asFloat(value));
            break;
          case "centerX":
            spin.setCenterX(asFloat(value));
            break;
          case "centerY":
            spin.setCenterY(asFloat(value));
            break;
          case "radius":
            spin.setRadius(asFloat(value));
            break;
          default:
            throw new IllegalArgumentException("Unknown SpinMarker field: " + fieldName);
        }
      }
    };
  }

  private static ComponentInspectorDescriptor spinMarkerDescriptor() {
    return () ->
        List.of(
            new FieldDescriptor("speedRadiansPerSecond", FieldKind.FLOAT, true),
            new FieldDescriptor("centerX", FieldKind.FLOAT, true),
            new FieldDescriptor("centerY", FieldKind.FLOAT, true),
            new FieldDescriptor("radius", FieldKind.FLOAT, true),
            new FieldDescriptor("angleRadians", FieldKind.FLOAT, false));
  }

  private static float asFloat(Object value) {
    if (value instanceof Number) {
      return ((Number) value).floatValue();
    }
    return Float.parseFloat(value.toString());
  }
}
