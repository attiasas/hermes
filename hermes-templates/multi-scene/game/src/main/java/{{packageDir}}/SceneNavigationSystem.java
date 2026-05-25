package

{{package}};

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneManager;

/**
 * Pushes the pause scene on an interval, then pops it on the next.
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
    public void update(World world, float deltaSeconds) {
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
