package dev.hermes.sample;

import dev.hermes.api.Component;

/** Orbits an entity around a center point. Registered via ServiceLoader only. */
public final class SpinMarker implements Component {

  private float speedRadiansPerSecond;
  private float centerX;
  private float centerY;
  private float radius;
  private float angleRadians;

  public float speedRadiansPerSecond() {
    return speedRadiansPerSecond;
  }

  public void setSpeedRadiansPerSecond(float speedRadiansPerSecond) {
    this.speedRadiansPerSecond = speedRadiansPerSecond;
  }

  public float centerX() {
    return centerX;
  }

  public void setCenterX(float centerX) {
    this.centerX = centerX;
  }

  public float centerY() {
    return centerY;
  }

  public void setCenterY(float centerY) {
    this.centerY = centerY;
  }

  public float radius() {
    return radius;
  }

  public void setRadius(float radius) {
    this.radius = radius;
  }

  public float angleRadians() {
    return angleRadians;
  }

  public void setAngleRadians(float angleRadians) {
    this.angleRadians = angleRadians;
  }
}
