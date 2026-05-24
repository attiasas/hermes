package dev.hermes.sample;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.MaterialUniform;
import dev.hermes.api.ecs.Mesh;
import dev.hermes.api.ecs.World;
import dev.hermes.api.render.RenderPass;

import java.util.HashMap;
import java.util.Map;

/**
 * Updates {@code u_time} on water materials before the world3d pass draws them.
 */
public final class WaterPass implements RenderPass {

    static final String WATER_SHADER = "water";
    static final String TIME_UNIFORM = "u_time";

    private float time;

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render(World world) {
        time += 0.016f;
        for (Entity entity : world.entitiesWith(Mesh.class)) {
            Material material = world.getComponent(entity.id(), Material.class);
            if (material == null || !WATER_SHADER.equals(material.shader())) {
                continue;
            }
            Map<String, MaterialUniform> uniforms = new HashMap<>(material.uniforms());
            uniforms.put(TIME_UNIFORM, new MaterialUniform(new float[]{time}));
            material.setUniforms(uniforms);
        }
    }

    @Override
    public void dispose() {
    }
}
