package dev.hermes.api.ecs;

/** Shader uniform value deserialized from JSON (float array or scalar). */
public final class MaterialUniform {

  private final float[] values;

  public MaterialUniform(float[] values) {
    this.values = values == null ? new float[0] : values;
  }

  public float[] getAsFloatArray() {
    return values;
  }
}
