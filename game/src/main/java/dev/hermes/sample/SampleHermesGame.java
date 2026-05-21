package dev.hermes.sample;

import dev.hermes.api.HermesApplication;
import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.SystemScope;

/**
 * Internal sample; compiles only against {@code hermes-api} (no libGDX imports).
 *
 * <p>Scene entities come from {@code src/main/resources/assets/scenes/}. {@code main} is bootstrapped by the
 * launcher; {@code pause} is registered here and pushed by {@link SceneNavigationSystem}.
 */
public final class SampleHermesGame implements HermesApplication {

  @Override
  public HermesSession createSession() {
    return new SampleHermesSession();
  }

  @Override
  public void onCreate(HermesEngine engine) {
    engine
        .registry()
        .register(
            "BounceMarker",
            BounceMarker.class,
            data -> {
              BounceMarker bounce = new BounceMarker();
              bounce.setAmplitude(data.getFloat("amplitude", 20f));
              bounce.setSpeed(data.getFloat("speed", 2f));
              return bounce;
            });
    engine.scenes().registry().register("pause", "scenes/pause.json");
    engine.addSystem(new BounceMarkerSystem(), SystemScope.ACTIVE_SCENE);
    engine.addSystem(new SceneNavigationSystem(engine.scenes(), 4f));
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void render() {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void dispose() {}

  /** Stub session for cross-scene state; replace when save/audio services land. */
  private static final class SampleHermesSession implements HermesSession {}
}
