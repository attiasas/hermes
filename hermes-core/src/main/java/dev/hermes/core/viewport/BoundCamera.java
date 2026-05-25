package dev.hermes.core.viewport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.SceneCamera;
import dev.hermes.api.math.Rect4;

/**
 * Camera bound to a render surface for one draw pass.
 */
public final class BoundCamera {

    private final SceneCamera sceneCamera;
    private final ActiveCamera active;
    private final RenderSurface surface;
    private final Rect4 viewportRect;

    BoundCamera(SceneCamera sceneCamera, ActiveCamera active, RenderSurface surface, Rect4 viewportRect) {
        this.sceneCamera = sceneCamera;
        this.active = active;
        this.surface = surface;
        this.viewportRect = viewportRect;
    }

    public ActiveCamera active() {
        return active;
    }

    public RenderSurface surface() {
        return surface;
    }

    public Rect4 viewportRect() {
        return viewportRect;
    }

    public Matrix4 combined() {
        return sceneCamera.combined();
    }

    public Camera gdxCamera() {
        return sceneCamera.gdxCamera();
    }

    public SceneCamera sceneCamera() {
        return sceneCamera;
    }

    public void applyGlViewport() {
        sceneCamera.applyGlViewport(viewportRect);
    }
}
