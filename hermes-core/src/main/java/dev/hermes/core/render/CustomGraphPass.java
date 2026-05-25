package dev.hermes.core.render;

import dev.hermes.api.ecs.World;
import dev.hermes.api.render.RenderPass;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;
import dev.hermes.core.viewport.ViewportServiceImpl;

final class CustomGraphPass implements RenderGraphPass {

    private final String id;
    private final RenderPass delegate;
    private final ViewportServiceImpl viewport;

    CustomGraphPass(String id, RenderPass delegate, ViewportServiceImpl viewport) {
        this.id = id;
        this.delegate = delegate;
        this.viewport = viewport;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void resize(int width, int height) {
        delegate.resize(width, height);
    }

    @Override
    public void render(World world, RenderSurface surface, BoundCamera bound) {
        RenderContextImpl context = new RenderContextImpl(surface, bound, viewport);
        delegate.render(world, context);
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }
}
