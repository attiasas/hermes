package dev.hermes.core.viewport;

import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.SceneCamera;
import dev.hermes.api.math.Rect4;

/**
 * Single owner of {@link SceneCamera}; binds active camera state to a render surface each pass.
 */
public final class ViewportCameraBinder {

    private final SceneCamera sceneCamera = new SceneCamera();

    public BoundCamera bind(ActiveCamera active, RenderSurface surface) {
        Rect4 rect = surface.viewportRect();
        sceneCamera.apply(active, surface.pixelWidth(), surface.pixelHeight(), rect);
        return new BoundCamera(sceneCamera, active, surface, rect);
    }

    public SceneCamera sceneCamera() {
        return sceneCamera;
    }
}
