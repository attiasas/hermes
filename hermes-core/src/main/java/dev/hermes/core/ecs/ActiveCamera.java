package dev.hermes.core.ecs;

import dev.hermes.api.ecs.Camera;

/** Resolved view state for the active scene camera (or the engine default). */
public final class ActiveCamera {

  private final Camera.Projection projection;
  private final float x;
  private final float y;
  private final float z;
  private final float rotationX;
  private final float rotationY;
  private final float rotationZ;
  private final float zoom;
  private final float fieldOfView;
  private final float near;
  private final float far;
  private final float viewportWidth;
  private final float viewportHeight;

  public ActiveCamera(
      Camera.Projection projection,
      float x,
      float y,
      float z,
      float rotationX,
      float rotationY,
      float rotationZ,
      float zoom,
      float fieldOfView,
      float near,
      float far,
      float viewportWidth,
      float viewportHeight) {
    this.projection = projection;
    this.x = x;
    this.y = y;
    this.z = z;
    this.rotationX = rotationX;
    this.rotationY = rotationY;
    this.rotationZ = rotationZ;
    this.zoom = zoom;
    this.fieldOfView = fieldOfView;
    this.near = near;
    this.far = far;
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
  }

  public Camera.Projection projection() {
    return projection;
  }

  public float x() {
    return x;
  }

  public float y() {
    return y;
  }

  public float z() {
    return z;
  }

  float rotationX() {
    return rotationX;
  }

  float rotationY() {
    return rotationY;
  }

  float rotationZ() {
    return rotationZ;
  }

  float zoom() {
    return zoom;
  }

  float fieldOfView() {
    return fieldOfView;
  }

  float near() {
    return near;
  }

  float far() {
    return far;
  }

  float viewportWidth() {
    return viewportWidth;
  }

  float viewportHeight() {
    return viewportHeight;
  }
}
