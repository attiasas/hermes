package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Shader and uniform bindings for drawable entities ({@link Sprite}, {@link Mesh}). */
public final class Material implements Component {

  private String shader = "default/unlit";
  private Map<String, MaterialUniform> uniforms = Collections.emptyMap();

  public String shader() {
    return shader;
  }

  public void setShader(String shader) {
    this.shader = shader;
  }

  public Map<String, MaterialUniform> uniforms() {
    return uniforms;
  }

  public void setUniforms(Map<String, MaterialUniform> uniforms) {
    this.uniforms =
        uniforms == null || uniforms.isEmpty()
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new HashMap<>(uniforms));
  }

  public MaterialUniform uniform(String name) {
    return uniforms.get(name);
  }
}
