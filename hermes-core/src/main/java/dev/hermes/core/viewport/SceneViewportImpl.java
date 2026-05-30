package dev.hermes.core.viewport;

import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.math.CoordinateSpace;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.math.ScreenRay;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.math.Vec3;
import dev.hermes.api.viewport.SceneViewport;
import dev.hermes.core.ecs.ActiveCamera;

/**
 * Per-surface viewport facade using a bound {@link SceneCamera}.
 */
public final class SceneViewportImpl implements SceneViewport {

    private final BoundCamera bound;
    private final Vector3 scratch = new Vector3();

    public SceneViewportImpl(BoundCamera bound) {
        this.bound = bound;
    }

    /** Test and layout helper: viewport rect on a screen-sized surface. */
    public static SceneViewportImpl forRect(
            float viewportX, float viewportY, float viewportW, float viewportH, int surfaceW, int surfaceH) {
        Rect4 rect = new Rect4().set(viewportX, viewportY, viewportW, viewportH);
        RenderSurface surface = RenderSurface.screen(surfaceW, surfaceH, rect);
        ActiveCamera active =
                new ActiveCamera(
                        Camera.Projection.ORTHOGRAPHIC,
                        viewportW * 0.5f,
                        viewportH * 0.5f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        67f,
                        0.1f,
                        3000f,
                        surfaceW,
                        surfaceH,
                        ViewportFitMode.STRETCH,
                        surfaceW / (float) Math.max(surfaceH, 1),
                        Float.NaN,
                        Float.NaN,
                        Float.NaN,
                        null);
        return new SceneViewportImpl(new ViewportCameraBinder().bind(active, surface));
    }

    @Override
    public CoordinateSpace space() {
        return CoordinateSpace.SURFACE;
    }

    @Override
    public float surfaceWidth() {
        return bound.surface().pixelWidth();
    }

    @Override
    public float surfaceHeight() {
        return bound.surface().pixelHeight();
    }

    @Override
    public void viewportRect(Rect4 out) {
        Rect4 rect = bound.viewportRect();
        out.set(rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public void screenToWorld(float screenX, float screenY, float worldZ, Vec3 out) {
        com.badlogic.gdx.graphics.Camera gdx = bound.gdxCamera();
        Rect4 rect = bound.viewportRect();
        screenY = rect.height - screenY + 2f * rect.y;
        scratch.set(screenX, screenY, 0f);
        gdx.unproject(scratch);
        float nearX = scratch.x;
        float nearY = scratch.y;
        float nearZ = scratch.z;
        scratch.set(screenX, screenY, 1f);
        gdx.unproject(scratch);
        float dz = scratch.z - nearZ;
        if (Math.abs(dz) < 1e-6f) {
            out.set(nearX, nearY, nearZ);
            return;
        }
        float t = (worldZ - nearZ) / dz;
        out.set(nearX + (scratch.x - nearX) * t, nearY + (scratch.y - nearY) * t, worldZ);
    }

    @Override
    public void worldToScreen(float worldX, float worldY, float worldZ, Vec2 out) {
        scratch.set(worldX, worldY, worldZ);
        bound.gdxCamera().project(scratch);
        out.set(scratch.x, scratch.y);
    }

    @Override
    public void normalizedToSurface(float nx, float ny, Vec2 out) {
        Rect4 rect = bound.viewportRect();
        out.set(rect.x + nx * rect.width, rect.y + ny * rect.height);
    }

    @Override
    public void surfaceToNormalized(float sx, float sy, Vec2 out) {
        Rect4 rect = bound.viewportRect();
        out.set((sx - rect.x) / rect.width, (sy - rect.y) / rect.height);
    }

    @Override
    public ScreenRay screenRay(float screenX, float screenY) {
        scratch.set(screenX, screenY, 0f);
        bound.gdxCamera().unproject(scratch);
        float originX = scratch.x;
        float originY = scratch.y;
        float originZ = scratch.z;
        scratch.set(screenX, screenY, 1f);
        bound.gdxCamera().unproject(scratch);
        float dirX = scratch.x - originX;
        float dirY = scratch.y - originY;
        float dirZ = scratch.z - originZ;
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (len > 0f) {
            dirX /= len;
            dirY /= len;
            dirZ /= len;
        }
        return new ScreenRay(originX, originY, originZ, dirX, dirY, dirZ);
    }

    public BoundCamera bound() {
        return bound;
    }
}
