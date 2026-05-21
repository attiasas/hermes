package dev.hermes.core.ecs;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import dev.hermes.api.ecs.Camera;

/** Owns libGDX cameras and applies {@link ActiveCamera} view state each frame. */
public final class SceneCamera {

  private static final float ORTHO_EYE_Z = 100f;

  private final OrthographicCamera orthographic = new OrthographicCamera();
  private final PerspectiveCamera perspective = new PerspectiveCamera();
  private Camera.Projection projection = Camera.Projection.ORTHOGRAPHIC;

  public void resize(float windowWidth, float windowHeight) {
    orthographic.setToOrtho(false, windowWidth, windowHeight);
    perspective.viewportWidth = windowWidth;
    perspective.viewportHeight = windowHeight;
  }

  public void apply(ActiveCamera active) {
    projection = active.projection();
    if (projection == Camera.Projection.PERSPECTIVE) {
      applyPerspective(active);
    } else {
      applyOrthographic(active);
    }
  }

  public Camera.Projection projection() {
    return projection;
  }

  public Matrix4 combined() {
    return projection == Camera.Projection.PERSPECTIVE ? perspective.combined : orthographic.combined;
  }

  /** Active libGDX camera for batched 3D rendering. */
  public com.badlogic.gdx.graphics.Camera gdxCamera() {
    return projection == Camera.Projection.PERSPECTIVE ? perspective : orthographic;
  }

  private void applyOrthographic(ActiveCamera active) {
    float viewportWidth = active.viewportWidth();
    float viewportHeight = active.viewportHeight();
    orthographic.setToOrtho(false, viewportWidth, viewportHeight);
    orthographic.position.set(active.x(), active.y(), ORTHO_EYE_Z);
    orthographic.zoom = active.zoom() <= 0f ? 1f : active.zoom();
    orthographic.near = 1f;
    orthographic.far = Math.max(active.far(), ORTHO_EYE_Z + 1f);
    if (active.rotationZ() != 0f) {
      orthographic.up.set(0f, 1f, 0f);
      orthographic.direction.set(0f, 0f, -1f);
      orthographic.rotate(active.rotationZ());
    }
    orthographic.update();
  }

  private void applyPerspective(ActiveCamera active) {
    perspective.viewportWidth = active.viewportWidth();
    perspective.viewportHeight = active.viewportHeight();
    perspective.fieldOfView = active.fieldOfView();
    perspective.near = active.near();
    perspective.far = active.far();
    perspective.position.set(active.x(), active.y(), active.z());
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
    perspective.update();
  }
}
