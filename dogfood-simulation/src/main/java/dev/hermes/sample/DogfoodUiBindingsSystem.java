package dev.hermes.sample;

import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.ui.UiService;

/**
 * Demo HP bindings for world-attached {@code ui/hp-bar.json} on the gameplay scene.
 */
public final class DogfoodUiBindingsSystem implements System {

    private static final float HP_MAX = 100f;

    private final HermesEngine engine;
    private float elapsed;

    public DogfoodUiBindingsSystem(HermesEngine engine) {
        this.engine = engine;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        SceneHandle active = engine.scenes().active();
        if (active == null || !"game".equals(active.id())) {
            return;
        }
        elapsed += deltaSeconds;
        float hp = HP_MAX * (0.55f + 0.45f * (float) Math.sin(elapsed * 1.4f));
        UiService ui = engine.ui();
        ui.setBinding("entity.hp", hp);
        ui.setBinding("entity.hpMax", HP_MAX);
    }
}
