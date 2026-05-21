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

  private static final float SMOKE_DELTA_SECONDS = 1f / 60f;

  private final HermesApplication application;
  private final boolean smokeMode;
  private HermesEngineImpl engine;
  private SpriteBatch batch;
  private RenderSystem renderSystem;
  private int smokeFramesRemaining;

  public HermesGdxApplication(HermesApplication application) {
    this.application = application;
    smokeFramesRemaining = readSmokeFramesProperty();
    smokeMode = smokeFramesRemaining > 0;
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
    if (!smokeMode) {
      batch = new SpriteBatch();
      renderSystem = new RenderSystem(batch);
    }

    application.onCreate(engine);

    String scenePath = HermesLauncherSupport.gameScenePath();
    if (scenePath != null && !scenePath.isBlank()) {
      engine.loadScene(scenePath);
    }

    int width = windowWidth();
    int height = windowHeight();
    if (renderSystem != null) {
      renderSystem.resize(width, height);
      engine.addSystem(renderSystem);
    }

    application.resize(width, height);
  }

  private static int windowWidth() {
    if (Gdx.graphics != null) {
      return Gdx.graphics.getWidth();
    }
    return HermesLauncherSupport.windowWidth();
  }

  private static int windowHeight() {
    if (Gdx.graphics != null) {
      return Gdx.graphics.getHeight();
    }
    return HermesLauncherSupport.windowHeight();
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
    if (!smokeMode) {
      Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    float delta = Gdx.graphics != null ? Gdx.graphics.getDeltaTime() : SMOKE_DELTA_SECONDS;
    for (dev.hermes.api.ecs.System system : engine.systems()) {
      system.update(engine.world(), delta);
    }
    if (!smokeMode) {
      for (dev.hermes.api.ecs.System system : engine.systems()) {
        system.render(engine.world());
      }
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
