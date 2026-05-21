package dev.hermes.core.render.resource;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.MaterialUniform;
import java.util.Map;

/** Maps ECS {@link Material} uniform values onto a bound {@link ShaderProgram}. */
public final class MaterialUniformBinder {

  private MaterialUniformBinder() {}

  public static void apply(ShaderProgram program, Material material) {
    if (program == null || material == null) {
      return;
    }
    for (Map.Entry<String, MaterialUniform> entry : material.uniforms().entrySet()) {
      applyOne(program, entry.getKey(), entry.getValue());
    }
  }

  private static void applyOne(ShaderProgram program, String name, MaterialUniform uniform) {
    float[] values = uniform.getAsFloatArray();
    if (values == null || values.length == 0) {
      return;
    }
    switch (values.length) {
      case 1:
        program.setUniformf(name, values[0]);
        break;
      case 2:
        program.setUniformf(name, values[0], values[1]);
        break;
      case 3:
        program.setUniformf(name, values[0], values[1], values[2]);
        break;
      case 4:
        program.setUniformf(name, values[0], values[1], values[2], values[3]);
        break;
      case 16:
        program.setUniformMatrix(name, new Matrix4(values));
        break;
      default:
        break;
    }
  }
}
