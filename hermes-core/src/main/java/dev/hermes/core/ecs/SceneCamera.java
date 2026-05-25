package dev.hermes.core.ecs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.math.Rect4;

/**
 * Owns libGDX cameras and applies {@link ActiveCamera} view state each frame.
 */
public final class SceneCamera {

    private static final float ORTHO_EYE_Z = 100f;

    private final OrthographicCamera orthographic = new OrthographicCamera();
    private final PerspectiveCamera perspective = new PerspectiveCamera();
    private Camera.Projection projection = Camera.Projection.ORTHOGRAPHIC;
    private Rect4 lastViewportRect = new Rect4();

    public void resize(float windowWidth, float windowHeight) {
        orthographic.setToOrtho(false, windowWidth, windowHeight);
        perspective.viewportWidth = windowWidth;
        perspective.viewportHeight = windowHeight;
    }

    public void apply(ActiveCamera active) {
        Rect4 full = new Rect4().set(0f, 0f, active.viewportWidth(), active.viewportHeight());
        apply(active, active.viewportWidth(), active.viewportHeight(), full);
    }

    public void apply(ActiveCamera active, float surfaceW, float surfaceH, Rect4 viewportRect) {
        projection = active.projection();
        lastViewportRect.set(viewportRect.x, viewportRect.y, viewportRect.width, viewportRect.height);
        if (projection == Camera.Projection.PERSPECTIVE) {
            applyPerspective(active, viewportRect);
        } else {
            applyOrthographic(active, viewportRect);
        }
    }

    public void applyGlViewport(Rect4 rect) {
        Gdx.gl.glViewport((int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height);
    }

    public Camera.Projection projection() {
        return projection;
    }

    public Matrix4 combined() {
        return projection == Camera.Projection.PERSPECTIVE ? perspective.combined : orthographic.combined;
    }

    public com.badlogic.gdx.graphics.Camera gdxCamera() {
        return projection == Camera.Projection.PERSPECTIVE ? perspective : orthographic;
    }

    public Rect4 lastViewportRect() {
        return lastViewportRect;
    }

    private void applyOrthographic(ActiveCamera active, Rect4 rect) {
        float aspect = rect.height > 0f ? rect.width / rect.height : 1f;
        float worldH = active.viewportHeight() / Math.max(active.zoom(), 0.0001f);
        float worldW = worldH * aspect;
        orthographic.setToOrtho(false, worldW, worldH);
        orthographic.viewportWidth = rect.width;
        orthographic.viewportHeight = rect.height;
        orthographic.position.set(active.x(), active.y(), ORTHO_EYE_Z);
        orthographic.zoom = 1f;
        orthographic.near = 1f;
        orthographic.far = Math.max(active.far(), ORTHO_EYE_Z + 1f);
        if (active.rotationZ() != 0f) {
            orthographic.up.set(0f, 1f, 0f);
            orthographic.direction.set(0f, 0f, -1f);
            orthographic.rotate(active.rotationZ());
        }
        orthographic.update();
    }

    private void applyPerspective(ActiveCamera active, Rect4 rect) {
        perspective.viewportWidth = rect.width;
        perspective.viewportHeight = rect.height;
        perspective.fieldOfView = active.fieldOfView();
        perspective.near = active.near();
        perspective.far = active.far();
        perspective.position.set(active.x(), active.y(), active.z());
        if (active.hasLookAt()) {
            perspective.lookAt(active.lookAtX(), active.lookAtY(), active.lookAtZ());
            if (active.rotationX() != 0f) {
                perspective.rotate(active.rotationX(), 1f, 0f, 0f);
            }
            if (active.rotationY() != 0f) {
                perspective.rotate(active.rotationY(), 0f, 1f, 0f);
            }
            if (active.rotationZ() != 0f) {
                perspective.rotate(active.rotationZ(), 0f, 0f, 1f);
            }
        } else {
            perspective.up.set(0f, 1f, 0f);
            perspective.direction.set(0f, 0f, -1f);
            if (active.rotationX() != 0f) {
                perspective.rotate(active.rotationX(), 1f, 0f, 0f);
            }
            if (active.rotationY() != 0f) {
                perspective.rotate(active.rotationY(), 0f, 1f, 0f);
            }
            if (active.rotationZ() != 0f) {
                perspective.rotate(active.rotationZ(), 0f, 0f, 1f);
            }
        }
        perspective.update();
    }
}
