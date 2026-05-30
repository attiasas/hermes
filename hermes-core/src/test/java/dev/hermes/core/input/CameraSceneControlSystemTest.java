package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.input.InputButton;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class CameraSceneControlSystemTest {

    private HermesEngineImpl engine;
    private WorldManagerImpl manager;
    private CameraSceneControlSystem cameraControl;
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
        SceneLoader.load("scenes/perspective-orbit-test.json", manager.entities(), registry);
        cameraControl = new CameraSceneControlSystem(input);
    }

    @Test
    void emptyClickDrag_orbitsPerspectiveCamera() {
        Entity cam = CameraResolver.activeCameraEntity(manager.entities()).orElseThrow();
        float rotationYBefore = manager.entities().getComponent(cam.id(), Transform.class).rotationY();

        input.pollFrame(InputFrame.pointerJustPressed(320, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);
        input.pollFrame(InputFrame.pointerDrag(340, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);

        float rotationYAfter = manager.entities().getComponent(cam.id(), Transform.class).rotationY();
        assertNotEquals(rotationYBefore, rotationYAfter, 0.01f);
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
