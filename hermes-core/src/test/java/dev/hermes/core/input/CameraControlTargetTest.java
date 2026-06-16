package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.Test;

final class CameraControlTargetTest {

    @Test
    void target_usesSelectedEntityPosition() {
        WorldManagerImpl manager = new WorldManagerImpl();
        Entity cube = manager.entities().create("cube");
        manager.entities().addComponent(cube.id(), new Transform(3f, 1f, 0f));
        manager.entities().addComponent(cube.id(), new Selected());

        CameraControlTarget target = CameraControlTarget.resolve(manager);

        assertEquals(3f, target.x(), 0.001f);
        assertEquals(1f, target.y(), 0.001f);
        assertEquals(true, target.fromSelection());
    }

    @Test
    void target_fallsBackToSceneLookAt() {
        WorldManagerImpl manager = new WorldManagerImpl();
        manager.camera().sceneConfig().setLookAt(1f, 2f, 3f);

        CameraControlTarget target = CameraControlTarget.resolve(manager);

        assertEquals(1f, target.x(), 0.001f);
        assertEquals(2f, target.y(), 0.001f);
        assertEquals(3f, target.z(), 0.001f);
    }
}
