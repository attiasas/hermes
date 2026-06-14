package dev.hermes.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.HermesApplication;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.render.HermesRenderConfigurator;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneStackPolicy;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ui.UiServiceImpl;
import dev.hermes.core.log.CachingLoggerProvider;
import dev.hermes.core.log.GdxLogSink;
import dev.hermes.core.log.LoggingRuntime;
import dev.hermes.core.config.RuntimeConfigServiceImpl;
import dev.hermes.core.config.RuntimeConfigServices;
import dev.hermes.core.render.RenderPipelineExecutor;
import dev.hermes.core.viewport.BackbufferSize;
import dev.hermes.core.viewport.ViewportServiceImpl;

/**
 * libGDX {@link ApplicationListener} that delegates lifecycle to a {@link HermesApplication}. Rendering uses libGDX
 * only inside this module.
 */
public final class HermesGdxApplication implements ApplicationListener {

    static {
        Logs.install(new CachingLoggerProvider(new GdxLogSink()));
    }

    private static final Logger log = Logs.get(HermesGdxApplication.class);

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
            HermesRuntimeConfig.reload();
            RuntimeConfigServiceImpl runtimeConfig = new RuntimeConfigServiceImpl();
            application.configureRuntime(runtimeConfig.builder());
            runtimeConfig.applyOverrides();
            RuntimeConfigServices.install(runtimeConfig);
            LoggingRuntime.reinitialize();

            log.info("Creating Hermes engine...");
            engine = new HermesEngineImpl();
            engine.bindApplication(application);
            batch = new SpriteBatch();
            RenderPassRegistry passRegistry = new RenderPassRegistry();
            application.configureRendering(new HermesRenderConfigurator(passRegistry));
            renderPipeline =
                    new RenderPipelineExecutor(
                            batch,
                            HermesLauncherSupport.gameRenderPipelinePath(),
                            passRegistry,
                            (ViewportServiceImpl) engine.viewport(),
                            (UiServiceImpl) engine.ui());

            String scenePath = HermesLauncherSupport.gameScenePath();
            if (scenePath != null && !scenePath.isBlank()) {
                engine.scenes().registry().register("main", scenePath);
            }

            application.onCreate(engine);

            engine.entityTypes().scanAssets();

            String audioProfile = RuntimeConfigServices.get().gameAudioProfile();
            if (audioProfile != null && !audioProfile.isBlank()) {
                engine.audio().loadProfile(audioProfile);
            }

            if (scenePath != null && !scenePath.isBlank()) {
                engine.scenes().request(SceneChangeRequest.goTo("main"));
                engine.scenes().processPending();
            }

            int width = BackbufferSize.width();
            int height = BackbufferSize.height();
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
            renderPipeline.resize(BackbufferSize.width(), BackbufferSize.height());
        }
        application.resize(BackbufferSize.width(), BackbufferSize.height());
    }

    @Override
    public void render() {
        int width = BackbufferSize.width();
        int height = BackbufferSize.height();

        if (fatalErrorScreen != null && fatalErrorScreen.isActive()) {
            fatalErrorScreen.render(width, height);
            return;
        }

        try {
            if (engine != null) {
                engine.resources().tick();
            }
            engine.scenes().processPending();

            float delta = Gdx.graphics.getDeltaTime();
            engine.input().poll(delta);
            boolean hasActiveScene = engine.scenes().stackDepth() > 0;
            SceneStackPolicy stackPolicy = engine.scenes().stackPolicy();

            for (HermesEngineImpl.SystemEntry entry : engine.systems()) {
                if (entry.scope() == SystemScope.GLOBAL) {
                    updateGlobalSystem(entry, engine.scenes().updateScenes(), delta, hasActiveScene, stackPolicy);
                }
            }

            engine.audio()
                    .tick(
                            delta,
                            hasActiveScene ? engine.scenes().activeManager() : null,
                            width,
                            height);

            if (hasActiveScene) {
                WorldManager activeManager = engine.scenes().activeManager();
                for (HermesEngineImpl.SystemEntry entry : engine.systems()) {
                    if (entry.scope() == SystemScope.ACTIVE_SCENE) {
                        entry.system().update(activeManager, delta);
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
        log.error("Fatal error occurred", error);
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
                if (scene.manager() != null) {
                    entry.system().update(scene.manager(), delta);
                }
            }
            return;
        }
        SceneHandle active = scenes.get(scenes.size() - 1);
        if (active.manager() != null) {
            entry.system().update(active.manager(), delta);
        }
    }

    @Override
    public void dispose() {
        log.info("Disposing Hermes engine...");
        application.dispose();
        if (engine != null) {
            engine.dispose();
        }
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
