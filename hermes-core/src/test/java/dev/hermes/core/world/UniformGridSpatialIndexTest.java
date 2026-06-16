package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.world.spatial.UniformGridSpatialIndex;
import org.junit.jupiter.api.Test;

final class UniformGridSpatialIndexTest {

    @Test
    void localizedQueryVisitsFewEntities() {
        WorldManagerImpl manager = new WorldManagerImpl();
        var es = manager.entities();
        for (int i = 0; i < 100; i++) {
            var e = es.create("e" + i);
            es.addComponent(e.id(), new Transform(i * 10f, 0f));
        }
        UniformGridSpatialIndex index = new UniformGridSpatialIndex(128f);
        index.rebuild(es);

        assertTrue(index.queryNear(5f, 0f, 20f).size() < 10);
    }
}
