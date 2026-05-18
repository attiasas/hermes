package dev.hermes.sample;

import dev.hermes.api.HermesApplication;
import dev.hermes.api.ecs.HermesEngine;

/** Internal sample; compiles only against {@code hermes-api} (no libGDX imports). */
public final class SampleHermesGame implements HermesApplication {

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
    engine.addSystem(new BounceMarkerSystem());
  }

  @Override
  public void create() {}

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
}
