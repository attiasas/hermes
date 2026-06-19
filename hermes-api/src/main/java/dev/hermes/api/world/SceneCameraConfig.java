package dev.hermes.api.world;

import dev.hermes.api.ecs.ViewportFitMode;

/**
 * Per-scene camera configuration from scene JSON {@code "camera"} field.
 */
public final class SceneCameraConfig {

    public enum Projection {
        ORTHOGRAPHIC,
        PERSPECTIVE
    }

    private Projection projection = Projection.ORTHOGRAPHIC;
    private float x;
    private float y;
    private float z;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float zoom = 1f;
    private float fieldOfView = 67f;
    private float near = 0.1f;
    private float far = 3000f;
    private float viewportWidth;
    private float viewportHeight;
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

    public float x() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float y() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float z() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float rotationX() {
        return rotationX;
    }

    public void setRotationX(float rotationX) {
        this.rotationX = rotationX;
    }

    public float rotationY() {
        return rotationY;
    }

    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }

    public float rotationZ() {
        return rotationZ;
    }

    public void setRotationZ(float rotationZ) {
        this.rotationZ = rotationZ;
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

    public float viewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public float viewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
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
