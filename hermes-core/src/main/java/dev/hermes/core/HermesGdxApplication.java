package dev.hermes.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.HermesApplication;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneHandle;
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

  public HermesGdxApplication(HermesApplication application) {
    this.application = application;
  }

  @Override
  public void create() {
    engine = new HermesEngineImpl();
    engine.bindApplication(application);
    batch = new SpriteBatch();
    renderSystem = new RenderSystem(batch);

    String scenePath = HermesLauncherSupport.gameScenePath();
    if (scenePath != null && !scenePath.isBlank()) {
      engine.scenes().registry().register("main", scenePath);
      engine.scenes().request(SceneChangeRequest.goTo("main"));
      engine.scenes().processPending();
    }

    application.onCreate(engine);

    renderSystem.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    engine.addSystem(renderSystem, SystemScope.GLOBAL);

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

    for (SceneHandle scene : engine.scenes().visibleScenes()) {
      World world = scene.world();
      for (HermesEngineImpl.SystemEntry entry : engine.systems()) {
        entry.system().render(world);
      }
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
    application.dispose();
    if (renderSystem != null) {
      renderSystem.dispose();
    }
    if (batch != null) {
      batch.dispose();
    }
  }
}
