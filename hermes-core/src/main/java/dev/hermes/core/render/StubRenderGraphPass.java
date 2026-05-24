package dev.hermes.core.render;

import dev.hermes.api.ecs.World;

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
    public void render(World world) {
    }

    @Override
    public void dispose() {
    }
}
