package {{package}};

import dev.hermes.api.HermesApplication;
import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.HermesEngine;

/** Multi-scene sample: registers a pause overlay and toggles it on a timer. */
public final class Game implements HermesApplication {

  @Override
  public HermesSession createSession() {
    return HermesSession.EMPTY;
  }

  @Override
  public void onCreate(HermesEngine engine) {
    engine.scenes().registry().register("pause", "scenes/pause.json");
    engine.addSystem(new SceneNavigationSystem(engine.scenes(), 5f));
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
