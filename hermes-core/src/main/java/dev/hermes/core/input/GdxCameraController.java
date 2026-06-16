package dev.hermes.core.input;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.world.CameraControlsConfig;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;
import dev.hermes.core.viewport.ViewportCameraBinder;

/** libGDX camera math adapter (CameraInputController parity). */
public final class GdxCameraController {

    private final ViewportCameraBinder binder = new ViewportCameraBinder();
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmpV1 = new Vector3();
    private final float surfaceWidth;
    private final float surfaceHeight;

    public GdxCameraController(float surfaceWidth, float surfaceHeight) {
        this.surfaceWidth = Math.max(surfaceWidth, 1f);
        this.surfaceHeight = Math.max(surfaceHeight, 1f);
    }

    public ActiveCamera orbit(
            ActiveCamera in,
            float targetX,
            float targetY,
            float targetZ,
            float deltaX,
            float deltaY,
            CameraControlsConfig cfg) {
        PerspectiveCamera cam = bindPerspective(in);
        Vector3 target = tmp.set(targetX, targetY, targetZ);
        tmpV1.set(cam.direction).crs(cam.up).y = 0f;
        cam.rotateAround(target, tmpV1.nor(), deltaY * cfg.rotateAngle());
        cam.rotateAround(target, Vector3.Y, deltaX * -cfg.rotateAngle());
        cam.update();
        return fromGdx(cam, in, targetX, targetY, targetZ);
    }

    public ActiveCamera pan(
            ActiveCamera in,
            float targetX,
            float targetY,
            float targetZ,
            float deltaX,
            float deltaY,
            CameraControlsConfig cfg) {
        PerspectiveCamera cam = bindPerspective(in);
        cam.translate(tmpV1.set(cam.direction).crs(cam.up).nor().scl(-deltaX * cfg.translateUnits()));
        cam.translate(tmp.set(cam.up).scl(-deltaY * cfg.translateUnits()));
        targetX += tmpV1.x + tmp.x;
        targetY += tmpV1.y + tmp.y;
        targetZ += tmpV1.z + tmp.z;
        cam.update();
        return fromGdx(cam, in, targetX, targetY, targetZ);
    }

    public ActiveCamera dolly(
            ActiveCamera in,
            float targetX,
            float targetY,
            float targetZ,
            float deltaY,
            CameraControlsConfig cfg) {
        PerspectiveCamera cam = bindPerspective(in);
        cam.translate(tmp.set(cam.direction).scl(deltaY * cfg.translateUnits()));
        targetX += tmp.x;
        targetY += tmp.y;
        targetZ += tmp.z;
        cam.update();
        return fromGdx(cam, in, targetX, targetY, targetZ);
    }

    public ActiveCamera scrollZoom(
            ActiveCamera in,
            float targetX,
            float targetY,
            float targetZ,
            float scrollAmount,
            CameraControlsConfig cfg) {
        return dolly(in, targetX, targetY, targetZ, scrollAmount * cfg.scrollFactor(), cfg);
    }

    public static float normalizeDeltaX(float pixelDelta, float surfaceWidth) {
        return pixelDelta / Math.max(surfaceWidth, 1f);
    }

    public static float normalizeDeltaY(float pixelDelta, float surfaceHeight) {
        return pixelDelta / Math.max(surfaceHeight, 1f);
    }

    private PerspectiveCamera bindPerspective(ActiveCamera active) {
        RenderSurface surface =
                RenderSurface.screen(
                        (int) surfaceWidth,
                        (int) surfaceHeight,
                        new dev.hermes.api.math.Rect4().set(0f, 0f, surfaceWidth, surfaceHeight));
        BoundCamera bound = binder.bind(active, surface);
        com.badlogic.gdx.graphics.Camera gdxCam = bound.gdxCamera();
        if (!(gdxCam instanceof PerspectiveCamera)) {
            throw new IllegalStateException("GdxCameraController requires a perspective camera");
        }
        PerspectiveCamera perspective = (PerspectiveCamera) gdxCam;
        return perspective;
    }

    static ActiveCamera fromGdx(
            PerspectiveCamera cam,
            ActiveCamera template,
            float lookAtX,
            float lookAtY,
            float lookAtZ) {
        float px = cam.position.x;
        float py = cam.position.y;
        float pz = cam.position.z;
        float dx = px - lookAtX;
        float dy = py - lookAtY;
        float dz = pz - lookAtZ;
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float rotationX = 0f;
        float rotationY = 0f;
        if (dist > 1e-4f) {
            rotationX = (float) Math.toDegrees(Math.asin(Math.max(-1f, Math.min(1f, dy / dist))));
            rotationY = (float) Math.toDegrees(Math.atan2(dx, dz));
        }
        return new ActiveCamera(
                Camera.Projection.PERSPECTIVE,
                px,
                py,
                pz,
                rotationX,
                rotationY,
                template.rotationZ(),
                template.zoom(),
                template.fieldOfView(),
                template.near(),
                template.far(),
                template.viewportWidth(),
                template.viewportHeight(),
                template.fitMode(),
                template.designAspect(),
                lookAtX,
                lookAtY,
                lookAtZ,
                template.renderTarget());
    }

    static ActiveCamera perspectiveAt(
            float x,
            float y,
            float z,
            float rotationX,
            float rotationY,
            float rotationZ,
            float lookAtX,
            float lookAtY,
            float lookAtZ,
            float surfaceWidth,
            float surfaceHeight) {
        return new ActiveCamera(
                Camera.Projection.PERSPECTIVE,
                x,
                y,
                z,
                rotationX,
                rotationY,
                rotationZ,
                1f,
                67f,
                0.1f,
                3000f,
                surfaceWidth,
                surfaceHeight,
                ViewportFitMode.STRETCH,
                surfaceWidth / Math.max(surfaceHeight, 1f),
                lookAtX,
                lookAtY,
                lookAtZ,
                null);
    }
}
