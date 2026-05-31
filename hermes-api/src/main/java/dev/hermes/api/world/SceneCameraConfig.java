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
    private boolean active = true;
    private float zoom = 1f;
    private float fieldOfView = 67f;
    private float near = 0.1f;
    private float far = 3000f;
    private float viewportWidth;
    private float viewportHeight;
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
        this.projection = projection;
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

    public String renderTarget() {
        return renderTarget;
    }

    public void setRenderTarget(String renderTarget) {
        this.renderTarget = renderTarget;
    }

    public ViewportFitMode fitMode() {
        return fitMode;
    }
    
    public void setFitMode(ViewportFitMode fitMode) {
        this.fitMode = fitMode;
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
    
    
    public void setLookAtX(float lookAtX) {
        this.lookAtX = lookAtX;
    }

    public float lookAtY() {
        return lookAtY;
    }
    
    public float lookAtZ() {
        return lookAtZ;
    }
    
    public void setLookAtZ(float lookAtZ) {
        this.lookAtZ = lookAtZ;
    }
}
