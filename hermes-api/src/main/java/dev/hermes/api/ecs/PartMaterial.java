package dev.hermes.api.ecs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Optional per-part shader and uniform overrides.
 */
public final class PartMaterial {

    private String shader;
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
