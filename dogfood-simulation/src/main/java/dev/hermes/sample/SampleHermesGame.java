package dev.hermes.sample;

import dev.hermes.api.HermesApplication;
import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.render.HermesRenderConfigurator;
import dev.hermes.api.scene.SceneStackPolicy;

/**
 * Internal sample; compiles only against {@code hermes-api} (no libGDX imports).
 *
 * <p>Scene entities come from {@code src/main/resources/assets/scenes/}. {@code main} is bootstrapped by the
 * launcher boots {@code main-menu.json} as scene id {@code main}; {@code game} and {@code pause} are registered here.
 */
public final class SampleHermesGame implements HermesApplication {

    private static final Logger log = Logs.get(SampleHermesGame.class);

    @Override
    public HermesSession createSession() {
        return new SampleHermesSession();
    }

    @Override
    public void configureRendering(HermesRenderConfigurator configurator) {
        configurator.registerPass("water", new WaterPass());
    }

    @Override
    public void onCreate(HermesEngine engine) {
        log.debug("Custom User application creating...");
        engine
                .registry()
                .register(
                        "BounceMarker",
                        BounceMarker.class,
                        (data, ctx) -> {
                            BounceMarker bounce = new BounceMarker();
                            bounce.setAmplitude(data.getFloat("amplitude", 20f));
                            bounce.setSpeed(data.getFloat("speed", 2f));
                            return bounce;
                        });
        engine.scenes().setStackPolicy(new SceneStackPolicy(true, true));
        engine.scenes().registry().register("game", "scenes/main.json");
        engine.scenes().registry().register("pause", "scenes/pause.json");
        engine.scenes().registry().register("advanced-render", "demos/advanced-render.json");
        engine.addSystem(new BounceMarkerSystem(), SystemScope.ACTIVE_SCENE);
        engine.addSystem(new MenuNavigationSystem(engine.scenes(), engine.input()));
        engine.addSystem(new DogfoodUiBindingsSystem(engine));
        engine.addSystem(new SceneNavigationSystem(engine.scenes(), 4f));
        engine.addSystem(new AdvancedRenderDemoSystem(engine.scenes(), 12f), SystemScope.GLOBAL);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    /**
     * Stub session for cross-scene state; replace when save/audio services land.
     */
    private static final class SampleHermesSession implements HermesSession {
    }
}
