package dev.hermes.core.viewport;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.math.ScreenRay;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.math.Vec3;
import dev.hermes.api.viewport.RenderSurfaceDesc;
import dev.hermes.api.viewport.SceneViewport;
import dev.hermes.api.viewport.ViewportService;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.render.FramebufferPool;

/**
 * Default {@link ViewportService} — coordinate authority for render surfaces and conversions.
 */
public final class ViewportServiceImpl implements ViewportService {

    private final ViewportCameraBinder binder = new ViewportCameraBinder();
    private int windowWidth = 1;
    private int windowHeight = 1;

    public ViewportCameraBinder binder() {
        return binder;
    }

    @Override
    public void onWindowResize(int w, int h) {
        windowWidth = Math.max(1, w);
        windowHeight = Math.max(1, h);
    }

    public RenderSurface surfaceForPass(String targetId, FramebufferPool pool, EntityStore entities) {
        int pw = pool.targetWidth(targetId);
        int ph = pool.targetHeight(targetId);
        ActiveCamera active = CameraResolver.resolveForPass(entities, targetId, pw, ph);
        Rect4 rect = new Rect4();
        ViewportLayout.compute(pw, ph, active.designAspect(), active.fitMode(), rect);
        if ("screen".equals(targetId)) {
            return RenderSurface.screen(pw, ph, rect);
        }
        return RenderSurface.framebuffer(targetId, pw, ph, rect);
    }

    @Override
    public int windowWidth() {
        return windowWidth;
    }

    @Override
    public int windowHeight() {
        return windowHeight;
    }

    @Override
    public RenderSurfaceDesc backbufferSurface(EntityStore entities) {
        ActiveCamera active = CameraResolver.resolveForPass(entities, "screen", windowWidth, windowHeight);
        Rect4 rect = new Rect4();
        ViewportLayout.compute(windowWidth, windowHeight, active.designAspect(), active.fitMode(), rect);
        return toDesc(RenderSurface.screen(windowWidth, windowHeight, rect));
    }

    @Override
    public SceneViewport forWorld(EntityStore entities) {
        return forSurface(entities, backbufferSurface(entities));
    }

    @Override
    public SceneViewport forWorld(EntityStore entities, String cameraEntityName) {
        ActiveCamera active =
                CameraResolver.resolveNamed(
                        entities, cameraEntityName, "screen", windowWidth, windowHeight);
        Rect4 rect = new Rect4();
        ViewportLayout.compute(windowWidth, windowHeight, active.designAspect(), active.fitMode(), rect);
        RenderSurface surface = RenderSurface.screen(windowWidth, windowHeight, rect);
        return new SceneViewportImpl(binder.bind(active, surface));
    }

    @Override
    public SceneViewport forSurface(EntityStore entities, RenderSurfaceDesc surfaceDesc) {
        ActiveCamera active =
                CameraResolver.resolveForPass(
                        entities,
                        surfaceDesc.targetId(),
                        surfaceDesc.pixelWidth(),
                        surfaceDesc.pixelHeight());
        RenderSurface surface = internalSurface(surfaceDesc);
        return new SceneViewportImpl(binder.bind(active, surface));
    }

    @Override
    public void mapScreenToSurface(
            float screenX, float screenY, RenderSurfaceDesc surface, Vec2 out) {
        Rect4 tmp = new Rect4();
        surface.viewportRect(tmp);
        float scaleX = surface.pixelWidth() / Math.max(1, windowWidth);
        float scaleY = surface.pixelHeight() / Math.max(1, windowHeight);
        float sx = screenX * scaleX;
        float sy = screenY * scaleY;
        out.set(sx, sy);
    }

    @Override
    public void screenToWorld(EntityStore entities, float screenX, float screenY, float worldZ, Vec3 out) {
        Vec2 mapped = new Vec2();
        mapScreenToSurface(screenX, screenY, backbufferSurface(entities), mapped);
        forWorld(entities).screenToWorld(mapped.x, mapped.y, worldZ, out);
    }

    @Override
    public void worldToScreen(EntityStore entities, float worldX, float worldY, float worldZ, Vec2 out) {
        forWorld(entities).worldToScreen(worldX, worldY, worldZ, out);
    }

    @Override
    public ScreenRay screenRay(EntityStore entities, float screenX, float screenY) {
        Vec2 mapped = new Vec2();
        mapScreenToSurface(screenX, screenY, backbufferSurface(entities), mapped);
        return forWorld(entities).screenRay(mapped.x, mapped.y);
    }

    private static RenderSurfaceDesc toDesc(RenderSurface surface) {
        Rect4 rect = surface.viewportRect();
        return new RenderSurfaceDesc(
                surface.targetId(), surface.pixelWidth(), surface.pixelHeight(), rect);
    }

    private static RenderSurface internalSurface(RenderSurfaceDesc desc) {
        Rect4 rect = new Rect4();
        desc.viewportRect(rect);
        if ("screen".equals(desc.targetId())) {
            return RenderSurface.screen(desc.pixelWidth(), desc.pixelHeight(), rect);
        }
        return RenderSurface.framebuffer(desc.targetId(), desc.pixelWidth(), desc.pixelHeight(), rect);
    }
}
