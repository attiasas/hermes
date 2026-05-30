package dev.hermes.core.input;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputService;
import dev.hermes.api.input.PickLayer;
import dev.hermes.api.input.PointerSnapshot;
import dev.hermes.core.ecs.CameraResolver;

/** GLOBAL system: empty left-drag orbits the active perspective camera around its look-at. */
public final class CameraSceneControlSystem implements System {

    private static final float DEG_PER_PIXEL = 0.35f;
    private static final float PITCH_CLAMP = 89f;

    private final InputService input;
    private boolean orbiting;
    private float lastScreenX;
    private float lastScreenY;

    public CameraSceneControlSystem(InputService input) {
        this.input = input;
    }

    @Override
    public void update(World world, float deltaSeconds) {
        PointerSnapshot ptr = input.devices().pointer();
        Camera.Projection mode =
                CameraResolver.activeCameraEntity(world)
                        .map(e -> world.getComponent(e.id(), Camera.class).projection())
                        .orElse(Camera.Projection.ORTHOGRAPHIC);
        if (mode != Camera.Projection.PERSPECTIVE) {
            orbiting = false;
            return;
        }
        if (ptr.justPressed(InputButton.LEFT)) {
            boolean hit =
                    input.pick(world, ptr.screenX(), ptr.screenY(), PickLayer.WORLD).isPresent();
            if (!hit) {
                orbiting = true;
                lastScreenX = ptr.screenX();
                lastScreenY = ptr.screenY();
            } else {
                orbiting = false;
            }
        }
        if (!ptr.pressed(InputButton.LEFT)) {
            orbiting = false;
            return;
        }
        if (!orbiting) {
            return;
        }
        float dx = ptr.screenX() - lastScreenX;
        float dy = ptr.screenY() - lastScreenY;
        lastScreenX = ptr.screenX();
        lastScreenY = ptr.screenY();
        orbitActiveCamera(world, dx, dy);
    }

    private void orbitActiveCamera(World world, float dx, float dy) {
        Entity cameraEntity = CameraResolver.activeCameraEntity(world).orElse(null);
        if (cameraEntity == null) {
            return;
        }
        Transform transform = world.getComponent(cameraEntity.id(), Transform.class);
        Camera camera = world.getComponent(cameraEntity.id(), Camera.class);
        if (transform == null || camera == null) {
            return;
        }

        float targetX;
        float targetY;
        float targetZ;
        if (hasLookAt(camera)) {
            targetX = camera.lookAtX();
            targetY = camera.lookAtY();
            targetZ = camera.lookAtZ();
        } else {
            targetX = 0f;
            targetY = 0f;
            targetZ = 0f;
        }

        float rotationY = transform.rotationY() + dx * DEG_PER_PIXEL;
        float rotationX = transform.rotationX() - dy * DEG_PER_PIXEL;
        rotationX = Math.max(-PITCH_CLAMP, Math.min(PITCH_CLAMP, rotationX));
        transform.setRotationY(rotationY);
        transform.setRotationX(rotationX);

        float offsetX = transform.x() - targetX;
        float offsetY = transform.y() - targetY;
        float offsetZ = transform.z() - targetZ;
        float distance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
        if (distance < 1e-4f) {
            distance = 5f;
        }

        float yawRad = (float) Math.toRadians(rotationY);
        float pitchRad = (float) Math.toRadians(rotationX);
        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float sinYaw = (float) Math.sin(yawRad);
        float cosYaw = (float) Math.cos(yawRad);

        transform.setX(targetX + distance * cosPitch * sinYaw);
        transform.setY(targetY + distance * sinPitch);
        transform.setZ(targetZ + distance * cosPitch * cosYaw);
        camera.setLookAt(targetX, targetY, targetZ);
    }

    private static boolean hasLookAt(Camera camera) {
        return !Float.isNaN(camera.lookAtX())
                && !Float.isNaN(camera.lookAtY())
                && !Float.isNaN(camera.lookAtZ());
    }
}
