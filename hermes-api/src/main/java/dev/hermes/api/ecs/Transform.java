package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/** World-space position, rotation (degrees), and scale for an entity. */
public final class Transform implements Component {

  private float x;
  private float y;
  private float z;
  private float rotationX;
  private float rotationY;
  private float rotationZ;
  private float scaleX = 1f;
  private float scaleY = 1f;
  private float scaleZ = 1f;

  public Transform() {}

  public Transform(float x, float y) {
    this(x, y, 0f);
  }

  public Transform(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public float x() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float y() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public float z() {
    return z;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public float rotationX() {
    return rotationX;
  }

  public void setRotationX(float rotationX) {
    this.rotationX = rotationX;
  }

  public float rotationY() {
    return rotationY;
  }

  public void setRotationY(float rotationY) {
    this.rotationY = rotationY;
  }

  public float rotationZ() {
    return rotationZ;
  }

  public void setRotationZ(float rotationZ) {
    this.rotationZ = rotationZ;
  }

  public float scaleX() {
    return scaleX;
  }

  public void setScaleX(float scaleX) {
    this.scaleX = scaleX;
  }

  public float scaleY() {
    return scaleY;
  }

  public void setScaleY(float scaleY) {
    this.scaleY = scaleY;
  }

  public float scaleZ() {
    return scaleZ;
  }

  public void setScaleZ(float scaleZ) {
    this.scaleZ = scaleZ;
  }
}
