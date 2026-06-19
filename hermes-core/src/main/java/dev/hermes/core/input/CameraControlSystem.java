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
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.SceneManagerImpl;
import dev.hermes.core.scene.SceneInstance;
import dev.hermes.core.ui.UiInputSystem;
import dev.hermes.core.ui.UiServiceImpl;
import dev.hermes.core.viewport.BackbufferSize;
import java.util.Optional;

/** GLOBAL system: built-in perspective camera controls (orbit / pan / dolly / scroll). */
public final class CameraControlSystem implements dev.hermes.api.ecs.System {

    private static final float DRAG_THRESHOLD_PX_SQ = 4f;

    private final InputService input;
    private final UiServiceImpl ui;
    private final SceneManagerImpl scenes;
    private final ViewportService viewport;

    private int activeButton = -1;
    private boolean pointerDragging;
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
            clearPointerDrag();
            return;
        }
        CameraControlsConfig cfg = manager.camera().controls();
        if (!cfg.enabled() || pointerOverUi(manager)) {
            clearPointerDrag();
            return;
        }
        float surfaceW = BackbufferSize.width();
        float surfaceH = BackbufferSize.height();
        if (surfaceW <= 0 || surfaceH <= 0) {
            return;
        }

        PointerSnapshot ptr = input.devices().pointer();
        trackActiveButton(ptr, cfg);
        endPointerDragIfNeeded(ptr);

        GdxCameraController gdx = new GdxCameraController(surfaceW, surfaceH);
        ActiveCamera active = CameraResolver.resolveForManager(manager, "screen", surfaceW, surfaceH);
        float targetX = lookAtX(active);
        float targetY = lookAtY(active);
        float targetZ = lookAtZ(active);

        ActiveCamera keyboardUpdated = applyKeyboardMovement(manager, gdx, active, deltaSeconds, cfg);
        if (keyboardUpdated != active) {
            active = keyboardUpdated;
            targetX = active.lookAtX();
            targetY = active.lookAtY();
            targetZ = active.lookAtZ();
        }

        if (cfg.scrollZoom() && Math.abs(ptr.scrollY()) > 1e-6f) {
            active = gdx.scrollZoom(active, targetX, targetY, targetZ, ptr.scrollY(), cfg);
            MainCameraWriter.write(
                    manager, active, active.lookAtX(), active.lookAtY(), active.lookAtZ());
            targetX = active.lookAtX();
            targetY = active.lookAtY();
            targetZ = active.lookAtZ();
        }

        if (activeButton < 0 || !ptr.pressed(activeButton)) {
            return;
        }

        float dx = ptr.screenX() - lastScreenX;
        float dy = lastScreenY - ptr.screenY();
        if (!pointerDragging) {
            if (dx * dx + dy * dy < DRAG_THRESHOLD_PX_SQ) {
                return;
            }
            pointerDragging = true;
        }

        if (dx == 0f && dy == 0f) {
            return;
        }

        lastScreenX = ptr.screenX();
        lastScreenY = ptr.screenY();

        active = CameraResolver.resolveForManager(manager, "screen", surfaceW, surfaceH);
        targetX = lookAtX(active);
        targetY = lookAtY(active);
        targetZ = lookAtZ(active);

        float normDx = GdxCameraController.normalizeDeltaX(dx, surfaceW);
        float normDy = GdxCameraController.normalizeDeltaY(dy, surfaceH);

        ActiveCamera updated = active;
        if (activeButton == cfg.rotateButton()) {
            updated = gdx.orbit(active, targetX, targetY, targetZ, normDx, normDy, cfg);
        } else if (activeButton == cfg.translateButton()) {
            updated = gdx.pan(active, targetX, targetY, targetZ, normDx, normDy, cfg);
        } else if (activeButton == cfg.forwardButton()) {
            updated = gdx.dolly(active, targetX, targetY, targetZ, normDy, cfg);
        }
        MainCameraWriter.write(
                manager, updated, updated.lookAtX(), updated.lookAtY(), updated.lookAtZ());
    }

    private ActiveCamera applyKeyboardMovement(
            WorldManager manager,
            GdxCameraController gdx,
            ActiveCamera active,
            float deltaSeconds,
            CameraControlsConfig cfg) {
        float forward = axis("camera_move_forward") - axis("camera_move_backward");
        float strafe = axis("camera_move_right") - axis("camera_move_left");
        float lookX = axis("camera_look_at_right") - axis("camera_look_at_left");
        float lookZ = axis("camera_look_at_forward") - axis("camera_look_at_backward");
        float lookY = axis("camera_look_at_up") - axis("camera_look_at_down");

        ActiveCamera updated = active;
        boolean changed = false;
        if (forward != 0f || strafe != 0f) {
            updated = gdx.translateView(updated, forward, strafe, deltaSeconds, cfg);
            changed = true;
        }
        if (lookX != 0f || lookY != 0f || lookZ != 0f) {
            updated = gdx.moveLookAt(updated, lookX, lookY, lookZ, deltaSeconds, cfg);
            changed = true;
        }
        if (changed) {
            MainCameraWriter.write(
                    manager, updated, updated.lookAtX(), updated.lookAtY(), updated.lookAtZ());
        }
        return updated;
    }

    private float axis(String action) {
        return input.actions().pressed(action) ? 1f : 0f;
    }

    private void clearPointerDrag() {
        activeButton = -1;
        pointerDragging = false;
    }

    private void endPointerDragIfNeeded(PointerSnapshot ptr) {
        if (activeButton < 0) {
            return;
        }
        if (!ptr.pressed(activeButton) || ptr.justReleased(activeButton)) {
            clearPointerDrag();
        }
    }

    private static float lookAtX(ActiveCamera active) {
        return active.hasLookAt() ? active.lookAtX() : 0f;
    }

    private static float lookAtY(ActiveCamera active) {
        return active.hasLookAt() ? active.lookAtY() : 0f;
    }

    private static float lookAtZ(ActiveCamera active) {
        return active.hasLookAt() ? active.lookAtZ() : 0f;
    }

    private void trackActiveButton(PointerSnapshot ptr, CameraControlsConfig cfg) {
        if (ptr.justPressed(cfg.rotateButton())) {
            activeButton = cfg.rotateButton();
            pointerDragging = false;
            lastScreenX = ptr.screenX();
            lastScreenY = ptr.screenY();
        } else if (ptr.justPressed(cfg.translateButton())) {
            activeButton = cfg.translateButton();
            pointerDragging = false;
            lastScreenX = ptr.screenX();
            lastScreenY = ptr.screenY();
        } else if (ptr.justPressed(cfg.forwardButton())) {
            activeButton = cfg.forwardButton();
            pointerDragging = false;
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
