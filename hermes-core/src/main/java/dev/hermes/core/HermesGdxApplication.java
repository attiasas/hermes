package dev.hermes.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.HermesApplication;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.RenderSystem;

/**
 * libGDX {@link ApplicationListener} that delegates lifecycle to a {@link HermesApplication}. Rendering uses libGDX
 * only inside this module.
 */
public final class HermesGdxApplication implements ApplicationListener {

  private final HermesApplication application;
  private HermesEngineImpl engine;
  private SpriteBatch batch;
  private RenderSystem renderSystem;
  private int smokeFramesRemaining;

  public HermesGdxApplication(HermesApplication application) {
    this.application = application;
    smokeFramesRemaining = readSmokeFramesProperty();
  }

  private static int readSmokeFramesProperty() {
    String value = System.getProperty("hermes.desktop.smokeFrames");
    if (value == null || value.isBlank()) {
      return 0;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  @Override
  public void create() {
    engine = new HermesEngineImpl();
    batch = new SpriteBatch();
    renderSystem = new RenderSystem(batch);

    application.onCreate(engine);

    String scenePath = HermesLauncherSupport.gameScenePath();
    if (scenePath != null && !scenePath.isBlank()) {
      engine.loadScene(scenePath);
    }

    renderSystem.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    engine.addSystem(renderSystem);

    application.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  @Override
  public void resize(int width, int height) {
    if (renderSystem != null) {
      renderSystem.resize(width, height);
    }
    application.resize(width, height);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    float delta = Gdx.graphics.getDeltaTime();
    for (dev.hermes.api.ecs.System system : engine.systems()) {
      system.update(engine.world(), delta);
    }
    for (dev.hermes.api.ecs.System system : engine.systems()) {
      system.render(engine.world());
    }

    application.render();

    if (smokeFramesRemaining > 0 && --smokeFramesRemaining == 0) {
      Gdx.app.exit();
    }
  }

  @Override
  public void pause() {
    application.pause();
  }

  @Override
  public void resume() {
    application.resume();
  }

  @Override
  public void dispose() {
    application.dispose();
    if (renderSystem != null) {
      renderSystem.dispose();
    }
    if (batch != null) {
      batch.dispose();
    }
  }
}
