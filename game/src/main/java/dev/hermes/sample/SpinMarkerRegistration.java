package dev.hermes.sample;

import dev.hermes.api.ecs.ComponentRegistration;
import dev.hermes.api.ecs.HermesEngine;

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
            });
    engine.addSystem(new SpinMarkerSystem());
  }
}
