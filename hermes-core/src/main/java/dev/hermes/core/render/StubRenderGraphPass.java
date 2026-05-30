package dev.hermes.core.render;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;

/**
 * No-op render pass used when validating pipeline structure in unit tests.
 */
final class StubRenderGraphPass implements RenderGraphPass {

    private final String id;

    StubRenderGraphPass(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render(EntityStore entities, RenderSurface surface, BoundCamera bound) {
    }

    @Override
    public void dispose() {
    }
}
