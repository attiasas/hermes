package dev.hermes.core.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import dev.hermes.api.HermesApplication;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.resource.ResourceManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SceneAsyncTransitionTest {

    private HermesEngineImpl engine;
    private ResourceManagerImpl resources;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
    }

    @BeforeEach
    void setUp() {
        engine = new HermesEngineImpl();
        engine.bindApplication(
                new HermesApplication() {
                    @Override
                    public void onCreate(HermesEngine ignored) {}

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
                });
        resources = (ResourceManagerImpl) engine.resources();
        resources.useCooperativeStrategyForTests(true);
        engine.scenes().registry().register("async", "scenes/async-preload-scene.json");
    }

    @Test
    void goToWithAsyncPreloadWaitsForBundleBeforeEnteringScene() {
        engine.scenes().request(SceneChangeRequest.goTo("async"));
        engine.scenes().processPending();

        assertNull(engine.scenes().active());
        assertEquals(0, engine.scenes().stackDepth());
        assertTrue(engine.scenes().loadingScreen().isVisible());

        pumpUntilTransitionComplete();

        assertFalse(engine.scenes().loadingScreen().isVisible());
        assertNotNull(engine.scenes().active());
        assertEquals("async", engine.scenes().active().id());
        assertNotNull(engine.scenes().activeManager().entities().findByName("async-marker"));
    }

    private void pumpUntilTransitionComplete() {
        for (int frame = 0; frame < 120 && engine.scenes().loadingScreen().isVisible(); frame++) {
            resources.tick();
            flushPostedRunnables();
            engine.scenes().processPending();
        }
        assertFalse(
                engine.scenes().loadingScreen().isVisible(),
                "Async scene transition did not complete within 120 frames");
    }

    private static void flushPostedRunnables() {
        if (Gdx.app instanceof HeadlessApplication) {
            ((HeadlessApplication) Gdx.app).executeRunnables();
        }
    }

}
