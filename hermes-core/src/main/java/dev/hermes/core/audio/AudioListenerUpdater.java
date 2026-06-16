package dev.hermes.core.audio;

import dev.hermes.api.ecs.WorldManager;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;

/** Updates the 3D audio listener from the active scene camera each frame. */
final class AudioListenerUpdater {

    private AudioListenerUpdater() {
    }

    static void update(
            SoundBackend backend, WorldManager manager, float surfaceWidth, float surfaceHeight) {
        if (backend == null || manager == null) {
            return;
        }
        ActiveCamera camera =
                CameraResolver.resolveForManager(manager, "screen", surfaceWidth, surfaceHeight);
        backend.setListenerPosition(camera.x(), camera.y(), camera.z());
    }
}
