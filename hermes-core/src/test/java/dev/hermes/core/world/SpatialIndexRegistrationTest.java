package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class SpatialIndexRegistrationTest {

    @BeforeAll
    static void loadSpi() {
        SpatialIndexRegistrations.loadServiceRegistrations();
    }

    @Test
    void serviceLoaderRegistersTestGridStrategy() {
        WorldManagerImpl manager = new WorldManagerImpl();
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);

        String json =
                "{ \"world\": { \"version\": 1, \"spatial\": { \"strategy\": \"testGrid\", \"cellSize\": 64 } },"
                        + " \"entities\": [] }";
        dev.hermes.core.ecs.SceneLoader.loadFromString("spi-test.json", json, manager, registry);

        assertInstanceOf(
                TestSpatialIndexRegistration.MarkerSpatialIndex.class, manager.space().spatial());
    }
}
