package dev.hermes.core.input;

import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputService;
import dev.hermes.api.input.PointerSnapshot;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.ui.UiNode;
import dev.hermes.api.viewport.RenderSurfaceDesc;
import dev.hermes.api.viewport.ViewportService;
import dev.hermes.api.world.CameraControlsConfig;
import dev.hermes.api.world.CameraControlsMode;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.SceneManagerImpl;
import dev.hermes.core.scene.SceneInstance;
import dev.hermes.core.ui.UiInputSystem;
import dev.hermes.core.ui.UiServiceImpl;
import dev.hermes.core.viewport.BackbufferSize;
import java.util.Optional;

/** GLOBAL system: built-in perspective camera controls (orbit / pan / dolly / FPS). */
public final class CameraControlSystem implements dev.hermes.api.ecs.System {

    private final InputService input;
    private final UiServiceImpl ui;
    private final SceneManagerImpl scenes;
    private final ViewportService viewport;

    private int activeButton = -1;
    private float lastScreenX;
    private float lastScreenY;

    public CameraControlSystem(InputService input) {
        this.input = input;
        this.ui = null;
        this.scenes = null;
        this.viewport = null;
    }

    public CameraControlSystem(HermesEngineImpl engine) {
        this.input = engine.input();
        this.ui = (UiServiceImpl) engine.ui();
        this.scenes = (SceneManagerImpl) engine.scenes();
        this.viewport = engine.viewport();
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        if (CameraResolver.mainCameraProjection(manager) != Camera.Projection.PERSPECTIVE) {
            activeButton = -1;
            return;
        }
        CameraControlsConfig cfg = manager.camera().controls();
        if (!cfg.enabled()) {
            activeButton = -1;
            return;
        }
        if (pointerOverUi(manager)) {
            activeButton = -1;
            return;
        }

        float surfaceW = BackbufferSize.width();
        float surfaceH = BackbufferSize.height();
        if (surfaceW <= 0 || surfaceH <= 0) {
            return;
        }

        PointerSnapshot ptr = input.devices().pointer();
        trackActiveButton(ptr, cfg);

        if (cfg.mode() == CameraControlsMode.FIRST_PERSON) {
            updateFirstPerson(manager, deltaSeconds, cfg, ptr, surfaceW, surfaceH);
            return;
        }
        updateOrbit(manager, deltaSeconds, cfg, ptr, surfaceW, surfaceH);
    }

    private void updateOrbit(
            WorldManager manager,
            float deltaSeconds,
            CameraControlsConfig cfg,
            PointerSnapshot ptr,
            float surfaceW,
            float surfaceH) {
        GdxCameraController gdx = new GdxCameraController(surfaceW, surfaceH);
        CameraControlTarget target = CameraControlTarget.resolve(manager);
        ActiveCamera active = CameraResolver.resolveForManager(manager, "screen", surfaceW, surfaceH);

        if (cfg.scrollZoom() && Math.abs(ptr.scrollY()) > 1e-6f) {
            active =
                    gdx.scrollZoom(
                            active,
                            target.x(),
                            target.y(),
                            target.z(),
                            ptr.scrollY(),
                            cfg);
            write(manager, active, target, cfg);
        }

        float forward = axis(input, "camera_forward") - axis(input, "camera_backward");
        if (forward != 0f) {
            active = CameraResolver.resolveForManager(manager, "screen", surfaceW, surfaceH);
            target = CameraControlTarget.resolve(manager);
            active =
                    gdx.dolly(active, target.x(), target.y(), target.z(), forward * deltaSeconds, cfg);
            write(manager, active, target, cfg);
        }

        if (activeButton < 0 || !ptr.pressed(activeButton)) {
            if (!ptr.pressed(activeButton)) {
                activeButton = -1;
            }
            return;
        }

        float dx = ptr.screenX() - lastScreenX;
        float dy = lastScreenY - ptr.screenY();
        lastScreenX = ptr.screenX();
        lastScreenY = ptr.screenY();
        if (dx == 0f && dy == 0f) {
            return;
        }

        float normDx = GdxCameraController.normalizeDeltaX(dx, surfaceW);
        float normDy = GdxCameraController.normalizeDeltaY(dy, surfaceH);
        active = CameraResolver.resolveForManager(manager, "screen", surfaceW, surfaceH);
        target = CameraControlTarget.resolve(manager);

        ActiveCamera updated = active;
        if (activeButton == cfg.rotateButton()) {
            updated =
                    gdx.orbit(
                            active,
                            target.x(),
                            target.y(),
                            target.z(),
                            normDx,
                            normDy,
                            cfg);
        } else if (activeButton == cfg.translateButton()) {
            updated =
                    gdx.pan(
                            active,
                            target.x(),
                            target.y(),
                            target.z(),
                            normDx,
                            normDy,
                            cfg,
                            target.fromSelection());
        } else if (activeButton == cfg.forwardButton()) {
            updated =
                    gdx.dolly(
                            active,
                            target.x(),
                            target.y(),
                            target.z(),
                            normDy,
                            cfg);
        }
        write(manager, updated, target, cfg);
    }

    private void updateFirstPerson(
            WorldManager manager,
            float deltaSeconds,
            CameraControlsConfig cfg,
            PointerSnapshot ptr,
            float surfaceW,
            float surfaceH) {
        GdxCameraController gdx = new GdxCameraController(surfaceW, surfaceH);
        ActiveCamera active = CameraResolver.resolveForManager(manager, "screen", surfaceW, surfaceH);
        CameraControlTarget target = CameraControlTarget.resolve(manager);

        if (activeButton == cfg.rotateButton() && ptr.pressed(activeButton)) {
            float dx = ptr.screenX() - lastScreenX;
            float dy = lastScreenY - ptr.screenY();
            lastScreenX = ptr.screenX();
            lastScreenY = ptr.screenY();
            if (dx != 0f || dy != 0f) {
                active =
                        gdx.firstPersonLook(
                                active,
                                -dx * cfg.degreesPerPixel(),
                                -dy * cfg.degreesPerPixel(),
                                cfg);
            }
        }

        float forward = axis(input, "camera_forward") - axis(input, "camera_backward");
        float strafe = axis(input, "camera_right") - axis(input, "camera_left");
        float vertical = axis(input, "camera_up") - axis(input, "camera_down");
        if (forward != 0f || strafe != 0f || vertical != 0f) {
            active = gdx.firstPersonMove(active, forward, strafe, vertical, deltaSeconds, cfg);
        }

        write(manager, active, target, cfg);
    }

    private static void write(
            WorldManager manager,
            ActiveCamera active,
            CameraControlTarget target,
            CameraControlsConfig cfg) {
        float lookX = target.fromSelection() ? target.x() : active.lookAtX();
        float lookY = target.fromSelection() ? target.y() : active.lookAtY();
        float lookZ = target.fromSelection() ? target.z() : active.lookAtZ();
        MainCameraWriter.write(manager, active, lookX, lookY, lookZ, cfg);
    }

    private static float axis(InputService input, String action) {
        return input.actions().pressed(action) ? 1f : 0f;
    }

    private void trackActiveButton(PointerSnapshot ptr, CameraControlsConfig cfg) {
        if (ptr.justPressed(cfg.rotateButton())) {
            activeButton = cfg.rotateButton();
            lastScreenX = ptr.screenX();
            lastScreenY = ptr.screenY();
        } else if (ptr.justPressed(cfg.translateButton())) {
            activeButton = cfg.translateButton();
            lastScreenX = ptr.screenX();
            lastScreenY = ptr.screenY();
        } else if (ptr.justPressed(cfg.forwardButton())) {
            activeButton = cfg.forwardButton();
            lastScreenX = ptr.screenX();
            lastScreenY = ptr.screenY();
        }
    }

    private boolean pointerOverUi(WorldManager manager) {
        if (ui == null || scenes == null || viewport == null) {
            return false;
        }
        PointerSnapshot ptr = input.devices().pointer();
        SceneHandle active = scenes.active();
        if (!(active instanceof SceneInstance)) {
            return false;
        }
        SceneInstance scene = (SceneInstance) active;
        if (scene.uiConfig().isEmpty()) {
            return false;
        }
        int surfaceW = BackbufferSize.width();
        int surfaceH = BackbufferSize.height();
        if (surfaceW <= 0 || surfaceH <= 0) {
            return false;
        }
        Optional<UiServiceImpl.LaidOutSceneUi> laidOut = ui.layoutScene(scene.id(), surfaceW, surfaceH);
        if (laidOut.isEmpty()) {
            return false;
        }
        RenderSurfaceDesc surface = viewport.backbufferSurface(manager.entities());
        Vec2 onSurface = new Vec2();
        viewport.mapScreenToSurface(ptr.screenX(), ptr.screenY(), surface, onSurface);
        UiNode hit =
                UiInputSystem.hitTest(
                        laidOut.get().layout(), laidOut.get().root(), onSurface.x, onSurface.y);
        return hit != null && "button".equals(hit.type());
    }
}
