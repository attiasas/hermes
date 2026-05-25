package dev.hermes.core.viewport;

import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.ecs.World;
import dev.hermes.api.math.CoordinateSpace;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.math.ScreenRay;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.math.Vec3;
import dev.hermes.api.viewport.SceneViewport;
import dev.hermes.core.ecs.SceneCamera;

/**
 * Per-surface viewport facade using a bound {@link SceneCamera}.
 */
public final class SceneViewportImpl implements SceneViewport {

    private final BoundCamera bound;
    private final Vector3 scratch = new Vector3();

    public SceneViewportImpl(BoundCamera bound) {
        this.bound = bound;
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
        scratch.set(screenX, screenY, worldZ);
        bound.gdxCamera().unproject(scratch);
        out.set(scratch.x, scratch.y, scratch.z);
    }

    @Override
    public void worldToScreen(float worldX, float worldY, float worldZ, Vec2 out) {
        scratch.set(worldX, worldY, worldZ);
        bound.gdxCamera().project(scratch);
        out.set(scratch.x, scratch.y);
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
