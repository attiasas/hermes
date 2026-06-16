package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.world.CameraControlsConfig;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class CameraControlSystemTest {

    private HermesEngineImpl engine;
    private WorldManagerImpl manager;
    private CameraControlSystem cameraControl;
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
        BuiltinComponents.register(registry);
        cameraControl = new CameraControlSystem(input);
    }

    @Test
    void emptyClickDrag_orbitsBoundEntityCamera() {
        var cam = manager.entities().create("cam");
        Camera camera = new Camera();
        camera.setProjection(Camera.Projection.PERSPECTIVE);
        camera.setLookAt(0f, 0f, 0f);
        camera.setFitMode(dev.hermes.api.ecs.ViewportFitMode.STRETCH);
        manager.entities().addComponent(cam.id(), camera);
        manager.entities().addComponent(cam.id(), new Transform(0f, 2f, 5f));
        manager.camera().bindMain("cam");
        manager.camera().setControls(CameraControlsConfig.defaults());

        Entity camEntity = CameraResolver.mainCameraEntity(manager).orElseThrow();
        float rotationYBefore =
                manager.entities().getComponent(camEntity.id(), Transform.class).rotationY();

        input.pollFrame(InputFrame.pointerJustPressed(320, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);
        input.pollFrame(InputFrame.pointerDrag(340, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);

        float rotationYAfter =
                manager.entities().getComponent(camEntity.id(), Transform.class).rotationY();
        assertNotEquals(rotationYBefore, rotationYAfter, 0.01f);
    }

    @Test
    void emptyClickDrag_orbitsSceneOwnedPerspectiveCamera() {
        SceneLoader.load(
                "scenes/perspective-orbit-test.json",
                manager,
                (ComponentRegistryImpl) engine.registry());

        float rotationYBefore = manager.camera().sceneConfig().rotationY();

        input.pollFrame(InputFrame.pointerJustPressed(320, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);
        input.pollFrame(InputFrame.pointerDrag(340, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);

        float rotationYAfter = manager.camera().sceneConfig().rotationY();
        assertNotEquals(rotationYBefore, rotationYAfter, 0.01f);
    }

    @Test
    void pointerMoveWithoutButton_doesNotMoveCamera() {
        SceneLoader.load(
                "scenes/perspective-orbit-test.json",
                manager,
                (ComponentRegistryImpl) engine.registry());
        float rotationYBefore = manager.camera().sceneConfig().rotationY();
        float zBefore = manager.camera().sceneConfig().z();

        input.pollFrame(InputFrame.builder().pointer(320f, 240f).build());
        cameraControl.update(manager, 0f);
        input.pollFrame(InputFrame.builder().pointer(360f, 280f).build());
        cameraControl.update(manager, 0f);

        assertEquals(rotationYBefore, manager.camera().sceneConfig().rotationY(), 0.001f);
        assertEquals(zBefore, manager.camera().sceneConfig().z(), 0.001f);
    }

    @Test
    void pointerRelease_stopsFurtherRotation() {
        SceneLoader.load(
                "scenes/perspective-orbit-test.json",
                manager,
                (ComponentRegistryImpl) engine.registry());

        input.pollFrame(InputFrame.pointerJustPressed(320, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);
        input.pollFrame(InputFrame.pointerDrag(360, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);
        float rotationAfterDrag = manager.camera().sceneConfig().rotationY();

        input.pollFrame(
                InputFrame.builder()
                        .pointer(360f, 240f)
                        .pointerJustReleased(InputButton.LEFT)
                        .build());
        cameraControl.update(manager, 0f);
        input.pollFrame(InputFrame.builder().pointer(420f, 240f).build());
        cameraControl.update(manager, 0f);

        assertEquals(rotationAfterDrag, manager.camera().sceneConfig().rotationY(), 0.001f);
    }

    @Test
    void clickWithoutDrag_doesNotRotate() {
        SceneLoader.load(
                "scenes/perspective-orbit-test.json",
                manager,
                (ComponentRegistryImpl) engine.registry());
        float rotationBefore = manager.camera().sceneConfig().rotationY();

        input.pollFrame(InputFrame.pointerJustPressed(320, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);
        input.pollFrame(InputFrame.pointerDrag(321, 240, InputButton.LEFT));
        cameraControl.update(manager, 0f);
        input.pollFrame(
                InputFrame.builder()
                        .pointer(321f, 240f)
                        .pointerJustReleased(InputButton.LEFT)
                        .build());
        cameraControl.update(manager, 0f);

        assertEquals(rotationBefore, manager.camera().sceneConfig().rotationY(), 0.001f);
    }

    @Test
    void wasd_translatesWithoutChangingRotation() {
        InputProfile profile =
                InputProfileLoader.parse(
                        "{"
                                + "\"version\":1,"
                                + "\"context\":\"gameplay\","
                                + "\"actions\":{"
                                + "\"camera_move_forward\":{\"type\":\"button\"},"
                                + "\"camera_move_backward\":{\"type\":\"button\"},"
                                + "\"camera_move_left\":{\"type\":\"button\"},"
                                + "\"camera_move_right\":{\"type\":\"button\"}"
                                + "},"
                                + "\"bindings\":["
                                + "{\"action\":\"camera_move_forward\",\"source\":\"keyboard\",\"key\":\"W\",\"when\":\"pressed\"},"
                                + "{\"action\":\"camera_move_backward\",\"source\":\"keyboard\",\"key\":\"S\",\"when\":\"pressed\"},"
                                + "{\"action\":\"camera_move_left\",\"source\":\"keyboard\",\"key\":\"A\",\"when\":\"pressed\"},"
                                + "{\"action\":\"camera_move_right\",\"source\":\"keyboard\",\"key\":\"D\",\"when\":\"pressed\"}"
                                + "]"
                                + "}");
        InputServiceImpl cameraInput = new InputServiceImpl(engine, profile);
        CameraControlSystem system = new CameraControlSystem(cameraInput);

        SceneLoader.load(
                "scenes/perspective-orbit-test.json",
                manager,
                (ComponentRegistryImpl) engine.registry());
        float rotationYBefore = manager.camera().sceneConfig().rotationY();
        float zBefore = manager.camera().sceneConfig().z();

        cameraInput.pollFrame(InputFrame.withKeyboardPressed(dev.hermes.api.input.InputKey.W));
        system.update(manager, 0.1f);

        assertNotEquals(zBefore, manager.camera().sceneConfig().z(), 0.01f);
        assertEquals(rotationYBefore, manager.camera().sceneConfig().rotationY(), 0.001f);
    }

    @Test
    void scrollZoom_movesSceneCameraCloser() {
        SceneLoader.load(
                "scenes/perspective-orbit-test.json",
                manager,
                (ComponentRegistryImpl) engine.registry());
        float zBefore = manager.camera().sceneConfig().z();

        input.pollFrame(InputFrame.pointerScroll(320, 240, -2f));
        cameraControl.update(manager, 0f);

        assertTrue(manager.camera().sceneConfig().z() < zBefore);
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
