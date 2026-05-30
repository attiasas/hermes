package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Material;
import org.junit.jupiter.api.Test;

final class EntitySpawnTest {

    @Test
    void spawn_createsEntityWithTemplateComponents() {
        WorldManagerImpl manager = managerWithSpinCubeType();
        Entity entity = manager.entities().spawn("spin-cube", "cube-1");
        assertEquals("spin-cube", entity.kind().id());
        assertEquals("cube-1", entity.name());
        assertNotNull(manager.entities().getComponent(entity.id(), Material.class));
    }

    private static WorldManagerImpl managerWithSpinCubeType() {
        EntityTypeRegistryImpl types = EntityTypeTestFixtures.registryWithSpinCube();
        ComponentRegistryImpl registry = EntityTypeTestFixtures.testRegistryWithMeshMaterialTransform();
        return new WorldManagerImpl(types, registry);
    }
}
