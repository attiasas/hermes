package dev.hermes.sample;

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneManager;

/**
 * Toggles a pause overlay on a timer (push on first interval, pop on the next).
 */
public final class SceneNavigationSystem implements System {

    private final SceneManager scenes;
    private final float intervalSeconds;
    private float elapsed;
    private boolean pauseVisible;

    public SceneNavigationSystem(SceneManager scenes, float intervalSeconds) {
        this.scenes = scenes;
        this.intervalSeconds = intervalSeconds;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        if (scenes.active() != null && !"game".equals(scenes.active().id())) {
            return;
        }
        elapsed += deltaSeconds;
        if (elapsed < intervalSeconds) {
            return;
        }
        elapsed = 0f;
        if (pauseVisible) {
            scenes.request(SceneChangeRequest.pop());
        } else {
            scenes.request(SceneChangeRequest.push("pause"));
        }
        pauseVisible = !pauseVisible;
    }
}
