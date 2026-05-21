package dev.hermes.sample;

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneManager;

/** Periodically switches between main and advanced-render to demo the water plane. */
public final class AdvancedRenderDemoSystem implements System {

  private final SceneManager scenes;
  private final float intervalSeconds;
  private float elapsed;
  private boolean showingAdvanced;

  public AdvancedRenderDemoSystem(SceneManager scenes, float intervalSeconds) {
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
    if (showingAdvanced) {
      scenes.request(SceneChangeRequest.goTo("main"));
    } else {
      scenes.request(SceneChangeRequest.goTo("advanced-render"));
    }
    showingAdvanced = !showingAdvanced;
  }
}
