package dev.hermes.core.ui;

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputService;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.ui.UiNode;
import dev.hermes.api.viewport.RenderSurfaceDesc;
import dev.hermes.api.viewport.ViewportService;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.SceneManagerImpl;
import dev.hermes.core.scene.SceneInstance;
import dev.hermes.core.viewport.BackbufferSize;
import java.util.List;
import java.util.Optional;

/** GLOBAL system: hit-tests the active scene UI tree and pulses button actions. */
public final class UiInputSystem implements System {

    private final UiActionPulse actionPulse;
    private final UiServiceImpl ui;
    private final SceneManagerImpl scenes;
    private final ViewportService viewport;
    private final InputService input;

    public UiInputSystem(UiActionPulse actionPulse) {
        this.actionPulse = actionPulse;
        this.ui = null;
        this.scenes = null;
        this.viewport = null;
        this.input = null;
    }

    public UiInputSystem(HermesEngineImpl engine) {
        this.actionPulse = engine.input()::pulseAction;
        this.ui = (UiServiceImpl) engine.ui();
        this.scenes = (SceneManagerImpl) engine.scenes();
        this.viewport = engine.viewport();
        this.input = engine.input();
    }

    /** Hit-tests {@code root} in SURFACE coordinates and pulses the topmost button action. */
    public void click(UiLayoutResult layout, UiNode root, float surfaceX, float surfaceY) {
        UiNode hit = hitTest(layout, root, surfaceX, surfaceY);
        activateButton(hit);
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        if (input == null || !input.devices().pointer().justPressed(InputButton.LEFT)) {
            return;
        }
        SceneHandle active = scenes.active();
        if (!(active instanceof SceneInstance)) {
            return;
        }
        SceneInstance scene = (SceneInstance) active;
        if (scene.uiConfig().isEmpty()) {
            return;
        }
        int surfaceW = BackbufferSize.width();
        int surfaceH = BackbufferSize.height();
        if (surfaceW <= 0 || surfaceH <= 0) {
            return;
        }
        Optional<UiServiceImpl.LaidOutSceneUi> laidOut = ui.layoutScene(scene.id(), surfaceW, surfaceH);
        if (laidOut.isEmpty()) {
            return;
        }
        float screenX = input.devices().pointer().screenX();
        float screenY = input.devices().pointer().screenY();
        RenderSurfaceDesc surface = viewport.backbufferSurface(manager.entities());
        Vec2 onSurface = new Vec2();
        viewport.mapScreenToSurface(screenX, screenY, surface, onSurface);
        UiNode hit = hitTest(laidOut.get().layout(), laidOut.get().root(), onSurface.x, onSurface.y);
        activateButton(hit);
    }

    private void activateButton(UiNode hit) {
        if (hit == null || !"button".equals(hit.type())) {
            return;
        }
        Object action = hit.prop("action");
        if (action instanceof String) {
            String actionName = (String) action;
            if (!actionName.isBlank()) {
                actionPulse.pulseAction(actionName);
            }
        }
    }

    static UiNode hitTest(UiLayoutResult layout, UiNode node, float surfaceX, float surfaceY) {
        List<UiNode> children = node.children();
        for (int i = children.size() - 1; i >= 0; i--) {
            UiNode hit = hitTest(layout, children.get(i), surfaceX, surfaceY);
            if (hit != null) {
                return hit;
            }
        }
        if ("button".equals(node.type()) && contains(layout, node, surfaceX, surfaceY)) {
            return node;
        }
        return null;
    }

    private static boolean contains(UiLayoutResult layout, UiNode node, float x, float y) {
        String id = node.id();
        if (id == null || id.isBlank()) {
            return false;
        }
        if (!layout.boundsById().containsKey(id)) {
            return false;
        }
        Rect4 bounds = layout.bounds(id);
        return x >= bounds.x()
                && x < bounds.x() + bounds.width()
                && y >= bounds.y()
                && y < bounds.y() + bounds.height();
    }
}
