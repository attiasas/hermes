package dev.hermes.core.render;

import dev.hermes.api.ecs.World;

/**
 * Wraps a render pass with framebuffer target bind/unbind.
 */
final class TargetBindingGraphPass implements RenderGraphPass {

    private final String target;
    private final RenderGraphPass delegate;
    private final FramebufferPool pool;

    TargetBindingGraphPass(String target, RenderGraphPass delegate, FramebufferPool pool) {
        this.target = target;
        this.delegate = delegate;
        this.pool = pool;
    }

    String target() {
        return target;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public void resize(int width, int height) {
        delegate.resize(width, height);
    }

    @Override
    public void render(World world) {
        pool.beginPass(target);
        try {
            delegate.render(world);
        } finally {
            pool.endPass(target);
        }
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }
}
