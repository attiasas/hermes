package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Transform;

import org.junit.jupiter.api.Test;

final class SceneEntityTypeTest {

    @Test
    void sceneTypedEntity_mergesOverrides() {
        String scene =
                "{\"entities\":[{\"type\":\"spin-cube\",\"id\":\"c1\","
                        + "\"components\":{\"Transform\":{\"x\":9}}}]}";
        WorldManagerImpl manager = loadScene(scene, EntityTypeTestFixtures.registryWithSpinCube());
        Entity c1 = manager.entities().findByName("c1");
        assertEquals("spin-cube", c1.kind().id());
        assertEquals(9f, manager.entities().getComponent(c1.id(), Transform.class).x(), 0.001f);
    }

    @Test
    void sceneKindAlias_sameAsType() {
        String scene = "{\"entities\":[{\"kind\":\"spin-cube\",\"id\":\"c1\"}]}";
        WorldManagerImpl manager = loadScene(scene, EntityTypeTestFixtures.registryWithSpinCube());
        assertEquals("spin-cube", manager.entities().findByName("c1").kind().id());
    }

    private static WorldManagerImpl loadScene(String json, EntityTypeRegistryImpl types) {
        WorldManagerImpl manager = new WorldManagerImpl();
        ComponentRegistryImpl registry = EntityTypeTestFixtures.testRegistryWithMeshMaterialTransform();
        SceneLoader.loadFromString("test/scene.json", json, manager.entities(), registry, types);
        return manager;
    }
}
