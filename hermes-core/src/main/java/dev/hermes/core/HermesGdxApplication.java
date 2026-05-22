package dev.hermes.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.HermesApplication;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.ecs.World;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.render.HermesRenderConfigurator;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneStackPolicy;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.log.CachingLoggerProvider;
import dev.hermes.core.log.GdxLogSink;
import dev.hermes.core.render.RenderPipelineExecutor;

/**
 * libGDX {@link ApplicationListener} that delegates lifecycle to a {@link HermesApplication}. Rendering uses libGDX
 * only inside this module.
 */
public final class HermesGdxApplication implements ApplicationListener {

  private final HermesApplication application;
  private HermesEngineImpl engine;
  private SpriteBatch batch;
  private RenderPipelineExecutor renderPipeline;
  private HermesFatalErrorScreen fatalErrorScreen;

  public HermesGdxApplication(HermesApplication application) {
    this.application = application;
  }

  @Override
  public void create() {
    fatalErrorScreen = new HermesFatalErrorScreen();
    try {
      Logs.install(new CachingLoggerProvider(new GdxLogSink()));
      Logger log = Logs.get(HermesGdxApplication.class);
      log.info("HermesGdxApplication created");
      engine = new HermesEngineImpl();
      engine.bindApplication(application);
      batch = new SpriteBatch();
      RenderPassRegistry passRegistry = new RenderPassRegistry();
      application.configureRendering(new HermesRenderConfigurator(passRegistry));
      renderPipeline =
          new RenderPipelineExecutor(
              batch, HermesLauncherSupport.gameRenderPipelinePath(), passRegistry);

      String scenePath = HermesLauncherSupport.gameScenePath();
      if (scenePath != null && !scenePath.isBlank()) {
        engine.scenes().registry().register("main", scenePath);
      }

      application.onCreate(engine);

      if (scenePath != null && !scenePath.isBlank()) {
        engine.scenes().request(SceneChangeRequest.goTo("main"));
        engine.scenes().processPending();
      }

      int width = Gdx.graphics.getWidth();
      int height = Gdx.graphics.getHeight();
      if (width <= 0 || height <= 0) {
        width = HermesLauncherSupport.windowWidth();
        height = HermesLauncherSupport.windowHeight();
      }
      renderPipeline.resize(width, height);

      application.resize(width, height);
    } catch (Throwable error) {
      enterFatalState(error);
    }
  }

  @Override
  public void resize(int width, int height) {
    if (fatalErrorScreen != null && fatalErrorScreen.isActive()) {
      return;
    }
    if (renderPipeline != null) {
      renderPipeline.resize(width, height);
    }
    application.resize(width, height);
  }

  @Override
  public void render() {
    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();
    if (width <= 0 || height <= 0) {
      width = HermesLauncherSupport.windowWidth();
      height = HermesLauncherSupport.windowHeight();
    }

    if (fatalErrorScreen != null && fatalErrorScreen.isActive()) {
      fatalErrorScreen.render(width, height);
      return;
    }

    try {
      engine.scenes().processPending();

      float delta = Gdx.graphics.getDeltaTime();
      boolean hasActiveScene = engine.scenes().stackDepth() > 0;
      SceneStackPolicy stackPolicy = engine.scenes().stackPolicy();

      for (HermesEngineImpl.SystemEntry entry : engine.systems()) {
        if (entry.scope() == SystemScope.GLOBAL) {
          updateGlobalSystem(entry, engine.scenes().updateScenes(), delta, hasActiveScene, stackPolicy);
        }
      }
      if (hasActiveScene) {
        World activeWorld = engine.scenes().activeWorld();
        for (HermesEngineImpl.SystemEntry entry : engine.systems()) {
          if (entry.scope() == SystemScope.ACTIVE_SCENE) {
            entry.system().update(activeWorld, delta);
          }
        }
      }

      renderPipeline.execute(engine.scenes().visibleScenes());

      application.render();
    } catch (Throwable error) {
      enterFatalState(error);
      fatalErrorScreen.render(width, height);
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

  private void enterFatalState(Throwable error) {
    if (fatalErrorScreen == null) {
      fatalErrorScreen = new HermesFatalErrorScreen();
    }
    fatalErrorScreen.report(error);
  }

  private static void updateGlobalSystem(
      HermesEngineImpl.SystemEntry entry,
      java.util.List<? extends SceneHandle> scenes,
      float delta,
      boolean hasActiveScene,
      SceneStackPolicy stackPolicy) {
    if (!hasActiveScene) {
      return;
    }
    if (stackPolicy.updateStackedScenes()) {
      for (SceneHandle scene : scenes) {
        if (scene.world() != null) {
          entry.system().update(scene.world(), delta);
        }
      }
      return;
    }
    SceneHandle active = scenes.get(scenes.size() - 1);
    if (active.world() != null) {
      entry.system().update(active.world(), delta);
    }
  }

  @Override
  public void dispose() {
    application.dispose();
    if (renderPipeline != null) {
      renderPipeline.dispose();
    }
    if (batch != null) {
      batch.dispose();
    }
    if (fatalErrorScreen != null) {
      fatalErrorScreen.dispose();
    }
  }
}
