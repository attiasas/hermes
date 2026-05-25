package dev.hermes.core.ecs;

import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.ViewportFitMode;

/**
 * Resolved view state for the active scene camera (or the engine default).
 */
public final class ActiveCamera {

    private final Camera.Projection projection;
    private final float x;
    private final float y;
    private final float z;
    private final float rotationX;
    private final float rotationY;
    private final float rotationZ;
    private final float zoom;
    private final float fieldOfView;
    private final float near;
    private final float far;
    private final float viewportWidth;
    private final float viewportHeight;
    private final ViewportFitMode fitMode;
    private final float designAspect;
    private final float lookAtX;
    private final float lookAtY;
    private final float lookAtZ;
    private final String renderTarget;

    public ActiveCamera(
            Camera.Projection projection,
            float x,
            float y,
            float z,
            float rotationX,
            float rotationY,
            float rotationZ,
            float zoom,
            float fieldOfView,
            float near,
            float far,
            float viewportWidth,
            float viewportHeight,
            ViewportFitMode fitMode,
            float designAspect,
            float lookAtX,
            float lookAtY,
            float lookAtZ,
            String renderTarget) {
        this.projection = projection;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.zoom = zoom;
        this.fieldOfView = fieldOfView;
        this.near = near;
        this.far = far;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.fitMode = fitMode == null ? ViewportFitMode.LETTERBOX : fitMode;
        this.designAspect = designAspect;
        this.lookAtX = lookAtX;
        this.lookAtY = lookAtY;
        this.lookAtZ = lookAtZ;
        this.renderTarget = renderTarget;
    }

    public Camera.Projection projection() {
        return projection;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    float rotationX() {
        return rotationX;
    }

    float rotationY() {
        return rotationY;
    }

    float rotationZ() {
        return rotationZ;
    }

    float zoom() {
        return zoom;
    }

    float fieldOfView() {
        return fieldOfView;
    }

    float near() {
        return near;
    }

    float far() {
        return far;
    }

    public float viewportWidth() {
        return viewportWidth;
    }

    public float viewportHeight() {
        return viewportHeight;
    }

    public ViewportFitMode fitMode() {
        return fitMode;
    }

    public float designAspect() {
        return designAspect;
    }

    public float lookAtX() {
        return lookAtX;
    }

    public float lookAtY() {
        return lookAtY;
    }

    public float lookAtZ() {
        return lookAtZ;
    }

    public boolean hasLookAt() {
        return !Float.isNaN(lookAtX) && !Float.isNaN(lookAtY) && !Float.isNaN(lookAtZ);
    }

    public String renderTarget() {
        return renderTarget;
    }
}
