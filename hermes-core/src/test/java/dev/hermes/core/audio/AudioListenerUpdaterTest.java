package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.Test;

final class AudioListenerUpdaterTest {

    @Test
    void listenerUsesActiveCameraTransform() {
        RecordingSoundBackend backend = new RecordingSoundBackend();
        WorldManagerImpl manager = new WorldManagerImpl();

        Entity cameraEntity = manager.entities().create("main-camera");
        Camera camera = new Camera();
        manager.entities().addComponent(cameraEntity.id(), camera);
        manager.entities().addComponent(cameraEntity.id(), new Transform(5f, 10f, 3f));
        manager.camera().bindMain("main-camera");

        AudioListenerUpdater.update(backend, manager, 800f, 600f);

        assertEquals(5f, backend.listenerX, 0.001f);
        assertEquals(10f, backend.listenerY, 0.001f);
        assertEquals(3f, backend.listenerZ, 0.001f);
    }
}
