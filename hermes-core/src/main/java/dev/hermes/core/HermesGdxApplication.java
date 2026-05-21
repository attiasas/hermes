package dev.hermes.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.HermesApplication;
import dev.hermes.core.debug.DebugOverlay;
import dev.hermes.core.debug.DebugRuntime;
import dev.hermes.core.debug.HermesDebugServer;
import dev.hermes.core.debug.WorldSnapshotBuilder;
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
  private DebugOverlay debugOverlay;
  private DebugRuntime debugRuntime;
  private HermesDebugServer debugServer;

  public HermesGdxApplication(HermesApplication application) {
    this.application = application;
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

    if (HermesLauncherSupport.isDebugEnabled()) {
      debugOverlay = new DebugOverlay(HermesLauncherSupport::isDebugEnabled);
      int port = HermesLauncherSupport.debugPort(18765);
      debugRuntime =
          new DebugRuntime(
              engine,
              new WorldSnapshotBuilder(engine.registryImpl()),
              HermesLauncherSupport::isDebugEnabled);
      debugServer = new HermesDebugServer(debugRuntime, port);
      debugServer.startIfEnabled();
    }
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
    if (debugRuntime == null || !debugRuntime.isPaused()) {
      for (dev.hermes.api.ecs.System system : engine.systems()) {
        system.update(engine.world(), delta);
      }
    }
    for (dev.hermes.api.ecs.System system : engine.systems()) {
      system.render(engine.world());
    }

    if (debugOverlay != null) {
      debugOverlay.render(engine.world());
    }

    application.render();
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
    if (debugServer != null) {
      debugServer.stop();
    }
    application.dispose();
    if (debugOverlay != null) {
      debugOverlay.dispose();
    }
    if (renderSystem != null) {
      renderSystem.dispose();
    }
    if (batch != null) {
      batch.dispose();
    }
  }
}
