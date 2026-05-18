package dev.hermes.sample;

import dev.hermes.api.Entity;
import dev.hermes.api.HermesApplication;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;

/**
 * Internal sample; compiles only against {@code hermes-api} (no libGDX imports).
 *
 * <p>Scene entities come from {@code src/main/resources/assets/scenes/main.json}. {@link #spawnCodeEntity(HermesEngine)}
 * adds one
 * entity from Java before the scene file is loaded.
 */
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
    spawnCodeEntity(engine);
  }

  private static void spawnCodeEntity(HermesEngine engine) {
    Entity marker = engine.world().createEntity("code-marker");
    Transform transform = new Transform(80f, 80f, 2f);
    transform.setRotationZ(15f);
    transform.setScaleX(0.75f);
    transform.setScaleY(0.75f);
    Sprite sprite = new Sprite("libgdx.png");
    BounceMarker bounce = new BounceMarker();
    bounce.setAmplitude(14f);
    bounce.setSpeed(2.8f);

    engine.world().addComponent(marker.id(), transform);
    engine.world().addComponent(marker.id(), sprite);
    engine.world().addComponent(marker.id(), bounce);
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
}
