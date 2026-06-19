package dev.hermes.api.world;

import dev.hermes.api.ecs.ViewportFitMode;

/** libGDX-free resolved main camera view for a render surface. */
public final class ActiveCameraView {

    private final SceneCameraConfig.Projection projection;
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

    public ActiveCameraView(
            SceneCameraConfig.Projection projection,
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
            float lookAtZ) {
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
        this.fitMode = fitMode;
        this.designAspect = designAspect;
        this.lookAtX = lookAtX;
        this.lookAtY = lookAtY;
        this.lookAtZ = lookAtZ;
    }

    public static ActiveCameraView fromSceneConfig(
            SceneCameraConfig config, float viewportWidth, float viewportHeight) {
        return new ActiveCameraView(
                config.projection(),
                config.x(),
                config.y(),
                config.z(),
                config.rotationX(),
                config.rotationY(),
                config.rotationZ(),
                config.zoom(),
                config.fieldOfView(),
                config.near(),
                config.far(),
                viewportWidth,
                viewportHeight,
                config.fitMode(),
                config.designAspect(),
                config.lookAtX(),
                config.lookAtY(),
                config.lookAtZ());
    }

    public SceneCameraConfig.Projection projection() {
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

    public float rotationX() {
        return rotationX;
    }

    public float rotationY() {
        return rotationY;
    }

    public float rotationZ() {
        return rotationZ;
    }

    public float zoom() {
        return zoom;
    }

    public float fieldOfView() {
        return fieldOfView;
    }

    public float near() {
        return near;
    }

    public float far() {
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
}
