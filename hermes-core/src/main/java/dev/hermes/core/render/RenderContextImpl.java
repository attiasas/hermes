package dev.hermes.core.render;

import dev.hermes.api.ecs.World;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.render.RenderContext;
import dev.hermes.api.viewport.SceneViewport;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;
import dev.hermes.core.viewport.SceneViewportImpl;
import dev.hermes.core.viewport.ViewportServiceImpl;

final class RenderContextImpl implements RenderContext {

    private final RenderSurface surface;
    private final BoundCamera bound;
    private final ViewportServiceImpl viewport;

    RenderContextImpl(RenderSurface surface, BoundCamera bound, ViewportServiceImpl viewport) {
        this.surface = surface;
        this.bound = bound;
        this.viewport = viewport;
    }

    @Override
    public String renderTargetId() {
        return surface.targetId();
    }

    @Override
    public float surfaceWidth() {
        return surface.pixelWidth();
    }

    @Override
    public float surfaceHeight() {
        return surface.pixelHeight();
    }

    @Override
    public void viewportRect(Rect4 out) {
        Rect4 rect = surface.viewportRect();
        out.set(rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public SceneViewport viewport(World world) {
        return new SceneViewportImpl(bound);
    }

    @Override
    public SceneViewport viewport(World world, String cameraEntityName) {
        ActiveCamera active =
                CameraResolver.resolveNamed(
                        world,
                        cameraEntityName,
                        surface.targetId(),
                        surface.pixelWidth(),
                        surface.pixelHeight());
        return new SceneViewportImpl(viewport.binder().bind(active, surface));
    }
}
