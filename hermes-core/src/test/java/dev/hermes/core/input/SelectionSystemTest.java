package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.math.Vec2;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SelectionSystemTest {

    private HermesEngineImpl engine;
    private WorldManagerImpl manager;
    private SelectionSystem selectionSystem;
    private InputServiceImpl input;

    @BeforeAll
    static void initGdx() {
        TestGdx.initHeadlessGl();
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        Gdx.graphics = new ResizableMockGraphics(640, 480);
        engine = new HermesEngineImpl();
        engine.viewport().onWindowResize(640, 480);
        input = (InputServiceImpl) engine.input();
        manager = new WorldManagerImpl();
        ComponentRegistryImpl registry = (ComponentRegistryImpl) engine.registry();
        SceneLoader.load("scenes/pick-test.json", manager.entities(), registry);
        selectionSystem = new SelectionSystem(input);
    }

    @Test
    void pointerClickSelectsExactlyOneEntity() {
        Entity near = manager.entities().findByName("near");
        Entity far = manager.entities().findByName("far");
        assertEquals(2, manager.entities().entitiesWith(dev.hermes.api.ecs.Selectable.class).size());

        Vec2 screen = new Vec2();
        engine.viewport().forWorld(manager.entities()).worldToScreen(165f, 240f, 0f, screen);

        input.pollFrame(InputFrame.pointerJustPressed(screen.x, screen.y, InputButton.LEFT));
        assertTrue(input.actions().justPressed("select"));

        selectionSystem.update(manager, 0f);

        assertEquals(1, manager.entities().entitiesWith(Selected.class).size());
        assertTrue(manager.entities().hasComponent(near.id(), Selected.class));
        assertFalse(manager.entities().hasComponent(far.id(), Selected.class));
    }

    @Test
    void pointerClickOnEmptyClearsSelection() {
        Entity near = manager.entities().findByName("near");
        manager.entities().addComponent(near.id(), new Selected());

        input.pollFrame(InputFrame.pointerJustPressed(10f, 10f, InputButton.LEFT));
        selectionSystem.update(manager, 0f);

        assertEquals(0, manager.entities().entitiesWith(Selected.class).size());
    }

    private static final class ResizableMockGraphics extends MockGraphics {
        private final int width;
        private final int height;

        ResizableMockGraphics(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getBackBufferWidth() {
            return width;
        }

        @Override
        public int getBackBufferHeight() {
            return height;
        }
    }
}
