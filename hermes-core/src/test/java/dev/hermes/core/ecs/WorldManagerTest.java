package dev.hermes.core.ecs;

import dev.hermes.api.ecs.EntityKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WorldManagerTest {

    @Test
    void manager_exposesEntityStore() {
        WorldManagerImpl manager = new WorldManagerImpl();
        var entity = manager.entities().create("a", EntityKind.UNSET);
        assertEquals(1, manager.entities().entityCount());
        assertEquals("a", entity.name());
    }
}
