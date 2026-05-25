package dev.hermes.core.render;

import dev.hermes.api.ecs.World;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;

interface RenderGraphPass {

    String id();

    void resize(int width, int height);

    void render(World world, RenderSurface surface, BoundCamera bound);

    void dispose();
}
