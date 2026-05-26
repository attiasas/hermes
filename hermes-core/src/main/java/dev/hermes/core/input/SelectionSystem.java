package dev.hermes.core.input;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Selected;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.World;
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
    public void update(World world, float deltaSeconds) {
        if (!input.actions().justPressed("select")) {
            return;
        }
        float screenX = input.devices().pointer().screenX();
        float screenY = input.devices().pointer().screenY();
        Optional<PickHit> hit = input.pick(world, screenX, screenY);
        clearSelected(world);
        if (hit.isPresent()) {
            world.addComponent(hit.get().entity, new Selected());
        }
    }

    private static void clearSelected(World world) {
        for (Entity entity : world.entitiesWith(Selected.class)) {
            world.removeComponent(entity.id(), Selected.class);
        }
    }
}
