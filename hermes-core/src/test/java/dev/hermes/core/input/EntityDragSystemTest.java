package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.input.InputButton;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class EntityDragSystemTest {

    private HermesEngineImpl engine;
    private WorldManagerImpl manager;
    private EntityDragSystem dragSystem;
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
        SceneLoader.load("scenes/ortho-drag-test.json", manager.entities(), registry);
        dragSystem = new EntityDragSystem(engine.viewport(), input);
    }

    @Test
    void dragMovesSelectedEntityInOrthoWorld() {
        Entity logo = manager.entities().findByName("logo");
        manager.entities().addComponent(logo.id(), new Selected());

        input.pollFrame(InputFrame.pointerPressed(100, 100, InputButton.LEFT));
        dragSystem.update(manager, 0f);
        input.pollFrame(InputFrame.pointerDrag(120, 100, InputButton.LEFT));
        dragSystem.update(manager, 0f);

        Transform t = manager.entities().getComponent(logo.id(), Transform.class);
        assertEquals(120f, t.x(), 0.5f);
    }

    @Test
    void dragIgnoredWhenSceneCameraIsPerspective() {
        ComponentRegistryImpl registry = (ComponentRegistryImpl) engine.registry();
        WorldManagerImpl perspectiveManager = new WorldManagerImpl();
        SceneLoader.load(
                "scenes/perspective-orbit-test.json",
                perspectiveManager,
                registry);

        Entity target = perspectiveManager.entities().create("cube");
        perspectiveManager.entities().addComponent(target.id(), new Transform(0f, 0f, 0f));
        perspectiveManager.entities().addComponent(target.id(), new Selected());

        input.pollFrame(InputFrame.pointerJustPressed(320, 240, InputButton.LEFT));
        dragSystem.update(perspectiveManager, 0f);
        input.pollFrame(InputFrame.pointerDrag(360, 240, InputButton.LEFT));
        dragSystem.update(perspectiveManager, 0f);

        Transform transform = perspectiveManager.entities().getComponent(target.id(), Transform.class);
        assertEquals(0f, transform.x(), 0.001f);
        assertEquals(0f, transform.y(), 0.001f);
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
