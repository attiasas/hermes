package dev.hermes.api.viewport;

import dev.hermes.api.math.CoordinateSpace;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.math.ScreenRay;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.math.Vec3;

/**
 * Per-world viewport facade for coordinate conversions on a bound render surface.
 */
public interface SceneViewport {

    CoordinateSpace space();

    float surfaceWidth();

    float surfaceHeight();

    /** Camera viewport rect on the current surface (pixel x, y, w, h). */
    void viewportRect(Rect4 out);

    void screenToWorld(float screenX, float screenY, float worldZ, Vec3 out);

    void worldToScreen(float worldX, float worldY, float worldZ, Vec2 out);

    ScreenRay screenRay(float screenX, float screenY);
}
