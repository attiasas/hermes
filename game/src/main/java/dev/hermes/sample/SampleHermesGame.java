package dev.hermes.sample;

import dev.hermes.api.Entity;
import dev.hermes.api.HermesApplication;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;

/**
 * Internal sample; compiles only against {@code hermes-api} (no libGDX imports).
 *
 * <p>Scene entities come from {@code assets/scenes/main.json}. The {@link #spawnCodeEntity()} entity below is
 * created in Java for documentation — same components, no JSON entry.
 */
public final class SampleHermesGame implements HermesApplication {

  private HermesEngine engine;

  @Override
  public void onCreate(HermesEngine engine) {
    this.engine = engine;
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
  public void create() {
    spawnCodeEntity();
  }

  /**
   * Spawns an entity entirely from code (not listed in the scene JSON). Runs in {@link #create()}, before the scene
   * file is loaded, so both JSON and code entities coexist in the world.
   */
  private void spawnCodeEntity() {
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
