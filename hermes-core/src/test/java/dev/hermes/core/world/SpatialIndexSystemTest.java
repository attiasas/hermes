package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.SpatialIndexSystem;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.Test;

final class SpatialIndexSystemTest {

    @Test
    void spawnAfterLoadIsQueryableWithoutManualRebuild() {
        WorldManagerImpl manager = new WorldManagerImpl();
        SpatialIndexSystem system = new SpatialIndexSystem();

        system.update(manager, 0.016f);
        assertEquals(0, manager.space().queryNear(0f, 0f, 1f).size());

        var entity = manager.entities().create("e");
        manager.entities().addComponent(entity.id(), new Transform(0f, 0f));
        system.update(manager, 0.016f);

        assertEquals(1, manager.space().queryNear(0f, 0f, 1f).size());
    }
}
