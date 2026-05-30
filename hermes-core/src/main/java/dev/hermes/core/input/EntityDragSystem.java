package dev.hermes.core.input;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputService;
import dev.hermes.api.input.PointerSnapshot;
import dev.hermes.api.math.Vec3;
import dev.hermes.api.viewport.ViewportService;
import dev.hermes.core.ecs.CameraResolver;

/** GLOBAL system: left-drag moves the selected entity in orthographic worlds. */
public final class EntityDragSystem implements System {

    private final ViewportService viewport;
    private final InputService input;
    private float lastScreenX;
    private float lastScreenY;
    private boolean anchored;

    public EntityDragSystem(ViewportService viewport, InputService input) {
        this.viewport = viewport;
        this.input = input;
    }

    @Override
    public void update(World world, float deltaSeconds) {
        PointerSnapshot ptr = input.devices().pointer();
        Camera.Projection mode =
                CameraResolver.activeCameraEntity(world)
                        .map(e -> world.getComponent(e.id(), Camera.class).projection())
                        .orElse(Camera.Projection.ORTHOGRAPHIC);
        if (mode != Camera.Projection.ORTHOGRAPHIC) {
            return;
        }
        Entity selected = findSelected(world);
        if (selected == null) {
            return;
        }
        if (!ptr.pressed(InputButton.LEFT)) {
            anchored = false;
            return;
        }
        Transform transform = world.getComponent(selected.id(), Transform.class);
        if (transform == null) {
            return;
        }

        float screenX = ptr.screenX();
        float screenY = ptr.screenY();
        if (!anchored) {
            lastScreenX = screenX;
            lastScreenY = screenY;
            anchored = true;
            return;
        }

        float planeZ = transform.z();
        Vec3 previous = new Vec3();
        Vec3 current = new Vec3();
        viewport.screenToWorld(world, lastScreenX, lastScreenY, planeZ, previous);
        viewport.screenToWorld(world, screenX, screenY, planeZ, current);
        transform.setX(transform.x() + (current.x - previous.x));
        transform.setY(transform.y() + (current.y - previous.y));
        lastScreenX = screenX;
        lastScreenY = screenY;
    }

    private static Entity findSelected(World world) {
        for (Entity entity : world.entitiesWith(Selected.class)) {
            return entity;
        }
        return null;
    }
}
