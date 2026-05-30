package dev.hermes.core.render;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;

interface RenderGraphPass {

    String id();

    void resize(int width, int height);

    void render(EntityStore entities, RenderSurface surface, BoundCamera bound);

    void dispose();
}
