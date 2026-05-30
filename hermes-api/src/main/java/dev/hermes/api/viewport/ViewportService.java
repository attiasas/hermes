package dev.hermes.api.viewport;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.math.ScreenRay;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.math.Vec3;

/**
 * Engine service for render surfaces, aspect-fit viewports, and coordinate conversions.
 */
public interface ViewportService {

    void onWindowResize(int width, int height);

    int windowWidth();

    int windowHeight();

    /** Backbuffer surface for the current window size and world's active camera fit policy. */
    RenderSurfaceDesc backbufferSurface(EntityStore entities);

    /** Viewport for the window backbuffer + active (or pass-matched) camera. */
    SceneViewport forWorld(EntityStore entities);

    SceneViewport forWorld(EntityStore entities, String cameraEntityName);

    /** Viewport for an explicit render target (FBO id or {@code "screen"}). */
    SceneViewport forSurface(EntityStore entities, RenderSurfaceDesc surface);

    /** Map window pointer into surface pixels (accounts for letterbox rect). */
    void mapScreenToSurface(float screenX, float screenY, RenderSurfaceDesc surface, Vec2 out);

    void screenToWorld(EntityStore entities, float screenX, float screenY, float worldZ, Vec3 out);

    void worldToScreen(EntityStore entities, float worldX, float worldY, float worldZ, Vec2 out);

    ScreenRay screenRay(EntityStore entities, float screenX, float screenY);
}
