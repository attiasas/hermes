package dev.hermes.sample;

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.input.InputService;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneManager;

/**
 * Handles menu UI actions: start gameplay, resume pause overlay, quit desktop run.
 */
public final class MenuNavigationSystem implements System {

    private final SceneManager scenes;
    private final InputService input;

    public MenuNavigationSystem(SceneManager scenes, InputService input) {
        this.scenes = scenes;
        this.input = input;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        if (input.actions().justPressed("start_game")) {
            scenes.request(SceneChangeRequest.goTo("game"));
            return;
        }
        if (input.actions().justPressed("resume")) {
            SceneHandle active = scenes.active();
            if (active != null && "pause".equals(active.id())) {
                scenes.request(SceneChangeRequest.pop());
            }
            return;
        }
        if (input.actions().justPressed("quit")) {
            java.lang.System.exit(0);
        }
    }
}
