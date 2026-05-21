package dev.hermes.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.HermesApplication;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.core.ecs.HermesEngineImpl;
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

  public HermesGdxApplication(HermesApplication application) {
    this.application = application;
  }

  @Override
  public void create() {
    engine = new HermesEngineImpl();
    engine.bindApplication(application);
    batch = new SpriteBatch();
    renderPipeline = new RenderPipelineExecutor(batch, HermesLauncherSupport.gameRenderPipelinePath());

    String scenePath = HermesLauncherSupport.gameScenePath();
    if (scenePath != null && !scenePath.isBlank()) {
      engine.scenes().registry().register("main", scenePath);
    }

    application.onCreate(engine);

    if (scenePath != null && !scenePath.isBlank()) {
      engine.scenes().request(SceneChangeRequest.goTo("main"));
      engine.scenes().processPending();
    }

    renderPipeline.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    application.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  @Override
  public void resize(int width, int height) {
    if (renderPipeline != null) {
      renderPipeline.resize(width, height);
    }
    application.resize(width, height);
  }

  @Override
  public void render() {
    engine.scenes().processPending();

    float delta = Gdx.graphics.getDeltaTime();
    World activeWorld = engine.scenes().activeWorld();
    boolean hasActiveScene = engine.scenes().stackDepth() > 0;

    for (HermesEngineImpl.SystemEntry entry : engine.systems()) {
      if (entry.scope() == SystemScope.GLOBAL) {
        entry.system().update(activeWorld, delta);
      }
    }
    if (hasActiveScene) {
      for (HermesEngineImpl.SystemEntry entry : engine.systems()) {
        if (entry.scope() == SystemScope.ACTIVE_SCENE) {
          entry.system().update(activeWorld, delta);
        }
      }
    }

    renderPipeline.execute(engine.scenes().visibleScenes());

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
    application.dispose();
    if (renderPipeline != null) {
      renderPipeline.dispose();
    }
    if (batch != null) {
      batch.dispose();
    }
  }
}
