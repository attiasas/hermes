package dev.hermes.core.ui;

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.viewport.ViewportService;

/** GLOBAL system: projects world-attached UI anchors each frame. */
public final class UiAttachSystem implements System {

    private final UiServiceImpl ui;
    private final ViewportService viewport;

    public UiAttachSystem(UiServiceImpl ui, ViewportService viewport) {
        this.ui = ui;
        this.viewport = viewport;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        ui.updateAttachLayouts(manager.entities(), viewport);
    }
}
