package dev.hermes.core.input;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.input.InputService;
import dev.hermes.api.input.PickHit;

import java.util.Optional;

/** GLOBAL system: pointer select action picks selectable entities in the active world. */
public final class SelectionSystem implements System {

    private final InputService input;

    public SelectionSystem(InputService input) {
        this.input = input;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        if (!input.actions().justPressed("select")) {
            return;
        }
        float screenX = input.devices().pointer().screenX();
        float screenY = input.devices().pointer().screenY();
        Optional<PickHit> hit = input.pick(entities, screenX, screenY);
        clearSelected(entities);
        if (hit.isPresent()) {
            entities.addComponent(hit.get().entity, new Selected());
        }
    }

    private static void clearSelected(EntityStore entities) {
        for (Entity entity : entities.entitiesWith(Selected.class)) {
            entities.removeComponent(entity.id(), Selected.class);
        }
    }
}
