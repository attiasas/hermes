package dev.hermes.core.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.world.WorldKind;
import dev.hermes.core.ecs.WorldManagerImpl;

public class WorldApiSurfaceTest {
    @Test
    void worldManagerExposesSpaceAndCamera() {
        WorldManagerImpl manager = new WorldManagerImpl();
        assertNotNull(manager.space());
        assertNotNull(manager.camera());
        assertEquals(WorldKind.OPEN, manager.space().kind());
        assertTrue(manager.space().bounds().unboundedX());
    }
}
