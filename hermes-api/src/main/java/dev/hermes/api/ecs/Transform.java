package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/** 2D position for an entity. */
public final class Transform implements Component {

  private float x;
  private float y;

  public Transform() {}

  public Transform(float x, float y) {
    this.x = x;
    this.y = y;
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
}
