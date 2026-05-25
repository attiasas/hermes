package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/**
 * Camera settings for an entity. Pair with {@link Transform} on the same entity for position and rotation.
 * Projection mode selects orthographic (2D) or perspective (3D) rendering.
 */
public final class Camera implements Component {

    public enum Projection {
        ORTHOGRAPHIC,
        PERSPECTIVE
    }

    private Projection projection = Projection.ORTHOGRAPHIC;
    private boolean active = true;
    private float zoom = 1f;
    private float fieldOfView = 67f;
    private float near = 0.1f;
    private float far = 3000f;
    private float viewportWidth;
    private float viewportHeight;
    /**
     * Optional pipeline framebuffer id; unset means eligible for any pass target.
     */
    private String renderTarget;
    private ViewportFitMode fitMode = ViewportFitMode.LETTERBOX;
    private float designAspect;
    private float lookAtX = Float.NaN;
    private float lookAtY = Float.NaN;
    private float lookAtZ = Float.NaN;

    public Projection projection() {
        return projection;
    }

    public void setProjection(Projection projection) {
        this.projection = projection == null ? Projection.ORTHOGRAPHIC : projection;
    }

    public boolean active() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public float zoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float fieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    public float near() {
        return near;
    }

    public void setNear(float near) {
        this.near = near;
    }

    public float far() {
        return far;
    }

    public void setFar(float far) {
        this.far = far;
    }

    /**
     * Viewport width in pixels; {@code 0} uses the window width.
     */
    public float viewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    /**
     * Viewport height in pixels; {@code 0} uses the window height.
     */
    public float viewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    /**
     * Optional render pipeline framebuffer id this camera renders into.
     * {@code null} or blank means the default pass target. Not yet applied by the engine.
     */
    public String renderTarget() {
        return renderTarget;
    }

    public void setRenderTarget(String renderTarget) {
        if (renderTarget == null || renderTarget.isBlank()) {
            this.renderTarget = null;
        } else {
            this.renderTarget = renderTarget.trim();
        }
    }

    public ViewportFitMode fitMode() {
        return fitMode;
    }

    public void setFitMode(ViewportFitMode fitMode) {
        this.fitMode = fitMode == null ? ViewportFitMode.LETTERBOX : fitMode;
    }

    public float designAspect() {
        return designAspect;
    }

    public void setDesignAspect(float designAspect) {
        this.designAspect = designAspect;
    }

  /** World point perspective camera aims at; NaN means use direction + rotation. */
    public float lookAtX() {
        return lookAtX;
    }

    public float lookAtY() {
        return lookAtY;
    }

    public float lookAtZ() {
        return lookAtZ;
    }

    public void setLookAt(float x, float y, float z) {
        this.lookAtX = x;
        this.lookAtY = y;
        this.lookAtZ = z;
    }
}
