package dev.hermes.sample;

import dev.hermes.api.Component;

/** Applies vertical sine motion on top of the current position. Registered in {@code onCreate} only. */
public final class BounceMarker implements Component {

  private float amplitude;
  private float speed;
  private float elapsedSeconds;
  private float baseY;
  private boolean baseYCaptured;

  public float amplitude() {
    return amplitude;
  }

  public void setAmplitude(float amplitude) {
    this.amplitude = amplitude;
  }

  public float speed() {
    return speed;
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

  public float elapsedSeconds() {
    return elapsedSeconds;
  }

  public void setElapsedSeconds(float elapsedSeconds) {
    this.elapsedSeconds = elapsedSeconds;
  }

  public float baseY() {
    return baseY;
  }

  public void setBaseY(float baseY) {
    this.baseY = baseY;
  }

  public boolean baseYCaptured() {
    return baseYCaptured;
  }

  public void setBaseYCaptured(boolean baseYCaptured) {
    this.baseYCaptured = baseYCaptured;
  }
}
