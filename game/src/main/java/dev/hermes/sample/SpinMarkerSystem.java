package dev.hermes.sample;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;

/** Moves entities with {@link SpinMarker} in a circle around their configured center. */
public final class SpinMarkerSystem implements System {

  @Override
  public void update(World world, float deltaSeconds) {
    for (Entity entity : world.entitiesWith(SpinMarker.class)) {
      SpinMarker spin = world.getComponent(entity.id(), SpinMarker.class);
      Transform transform = world.getComponent(entity.id(), Transform.class);
      if (spin == null || transform == null) {
        continue;
      }

      spin.setAngleRadians(spin.angleRadians() + spin.speedRadiansPerSecond() * deltaSeconds);
      float x = spin.centerX() + (float) Math.cos(spin.angleRadians()) * spin.radius();
      float y = spin.centerY() + (float) Math.sin(spin.angleRadians()) * spin.radius();
      transform.setX(x);
      transform.setY(y);
    }
  }
}
