package dev.hermes.core.render;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;
import dev.hermes.core.viewport.ViewportServiceImpl;

/**
 * Wraps a render pass with framebuffer target bind/unbind and viewport camera binding.
 */
final class TargetBindingGraphPass implements RenderGraphPass {

    private final String target;
    private final String cameraEntityName;
    private final RenderGraphPass delegate;
    private final FramebufferPool pool;
    private final ViewportServiceImpl viewport;

    TargetBindingGraphPass(
            String target,
            String cameraEntityName,
            RenderGraphPass delegate,
            FramebufferPool pool,
            ViewportServiceImpl viewport) {
        this.target = target;
        this.cameraEntityName = cameraEntityName;
        this.delegate = delegate;
        this.pool = pool;
        this.viewport = viewport;
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
    public void render(EntityStore entities, RenderSurface surface, BoundCamera ignored, String sceneId) {
        pool.beginPass(target);
        try {
            RenderSurface passSurface = viewport.surfaceForPass(target, pool, entities);
            ActiveCamera active = resolveCamera(entities, passSurface);
            BoundCamera bound = viewport.binder().bind(active, passSurface);
            bound.applyGlViewport();
            delegate.render(entities, passSurface, bound, sceneId);
        } finally {
            pool.endPass(target);
        }
    }

    private ActiveCamera resolveCamera(EntityStore entities, RenderSurface passSurface) {
        if (cameraEntityName != null && !cameraEntityName.isBlank()) {
            return CameraResolver.resolveNamed(
                    entities,
                    cameraEntityName,
                    target,
                    passSurface.pixelWidth(),
                    passSurface.pixelHeight());
        }
        return CameraResolver.resolveForPass(
                entities, target, passSurface.pixelWidth(), passSurface.pixelHeight());
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }
}
