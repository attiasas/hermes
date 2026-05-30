package dev.hermes.api.render;

import dev.hermes.api.ecs.EntityStore;

/**
 * Code-registered render pass invoked from a pipeline JSON {@code custom} pass.
 */
public interface RenderPass {

    void resize(int width, int height);

    void render(EntityStore entities, RenderContext context);

    void dispose();
}
