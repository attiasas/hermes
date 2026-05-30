package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.Transform;

import java.util.Map;

import org.junit.jupiter.api.Test;

final class EntityFactoryTest {

    @Test
    void spawnFromRegisteredType_appliesTemplateComponents() {
        EntityTypeRegistryImpl types = EntityTypeTestFixtures.registryWithSpinCube();
        ComponentRegistryImpl components = EntityTypeTestFixtures.testRegistryWithMeshMaterialTransform();
        EntityStoreImpl store = new EntityStoreImpl(new EntityFactory(types, components));
        EntityFactory factory = new EntityFactory(types, components);

        Entity entity = factory.create("test/path", store, "c1", "spin-cube", Map.of());

        assertEquals("spin-cube", entity.kind().id());
        assertTrue(store.hasComponent(entity.id(), Material.class));
        assertEquals(0f, store.getComponent(entity.id(), Transform.class).z(), 0.001f);
    }

    @Test
    void typedEntity_refUsesMergedInstanceOverride() {
        EntityTypeRegistryImpl types = EntityTypeTestFixtures.registryWithSpinCube();
        ComponentRegistryImpl components = EntityTypeTestFixtures.testRegistryWithMeshMaterialTransform();
        EntityStoreImpl store = new EntityStoreImpl(new EntityFactory(types, components));
        EntityFactory factory = new EntityFactory(types, components);
        JsonValue transformOverride = new JsonReader().parse("{\"x\":9}");

        Entity entity =
                factory.create(
                        "test/path",
                        store,
                        "c1",
                        "spin-cube",
                        Map.of("Transform", transformOverride));

        assertEquals(9f, store.getComponent(entity.id(), TestSpinMarker.class).centerX(), 0.001f);
    }
}
