package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputKey;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.scene.AssetSceneSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class InputServiceImplTest {

    private InputServiceImpl input;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        InputProfile profile = InputProfileLoader.load("input/profile.json");
        input = new InputServiceImpl(new HermesEngineImpl(), profile);
    }

    @Test
    void pollFrame_mapsSelectOnPointerClick() {
        input.pollFrame(InputFrame.pointerJustPressed(10, 20, InputButton.LEFT));
        assertTrue(input.actions().justPressed("select"));
        assertEquals(10f, input.devices().pointer().screenX(), 0.0001f);
        assertEquals(20f, input.devices().pointer().screenY(), 0.0001f);
    }

    @Test
    void pollFrame_usesSceneInputContextWhenActive() {
        String profileJson =
                "{"
                        + "\"version\":1,"
                        + "\"context\":\"gameplay\","
                        + "\"actions\":{\"select\":{\"type\":\"button\"}},"
                        + "\"bindings\":[{\"action\":\"select\",\"source\":\"pointer\","
                        + "\"button\":\"LEFT\",\"when\":\"justPressed\",\"context\":\"menu\"}]"
                        + "}";
        HermesEngineImpl engine = new HermesEngineImpl();
        InputServiceImpl sceneInput =
                new InputServiceImpl(engine, InputProfileLoader.parse(profileJson));
        engine.scenes().registry().register(
                SceneDefinition.builder("menu")
                        .source(new AssetSceneSource("scenes/with-input-context.json"))
                        .build());
        engine.scenes().request(SceneChangeRequest.goTo("menu"));
        engine.scenes().processPending();

        sceneInput.pollFrame(InputFrame.pointerJustPressed(0, 0, InputButton.LEFT));
        assertTrue(sceneInput.actions().justPressed("select"));
        assertEquals("menu", engine.scenes().active().inputContext().orElseThrow());
    }

    @Test
    void pollFrame_skipsContextScopedBindingWhenSceneContextDiffers() {
        String profileJson =
                "{"
                        + "\"version\":1,"
                        + "\"context\":\"gameplay\","
                        + "\"actions\":{\"select\":{\"type\":\"button\"}},"
                        + "\"bindings\":[{\"action\":\"select\",\"source\":\"pointer\","
                        + "\"button\":\"LEFT\",\"when\":\"justPressed\",\"context\":\"menu\"}]"
                        + "}";
        HermesEngineImpl engine = new HermesEngineImpl();
        InputServiceImpl sceneInput =
                new InputServiceImpl(engine, InputProfileLoader.parse(profileJson));
        engine.scenes().registry().register(
                SceneDefinition.builder("game")
                        .source(ctx -> {})
                        .build());
        engine.scenes().request(SceneChangeRequest.goTo("game"));
        engine.scenes().processPending();

        sceneInput.pollFrame(InputFrame.pointerJustPressed(0, 0, InputButton.LEFT));
        assertFalse(sceneInput.actions().justPressed("select"));
    }

    @Test
    void pollFrame_mapsMoveXFromKeyboard() {
        input.pollFrame(InputFrame.withKeyboardPressed(InputKey.D));
        assertEquals(1f, input.actions().axis("move_x"), 0.0001f);
        assertTrue(input.devices().keyboard().pressed(InputKey.D));
    }
}
