package dev.hermes.core.world;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.world.SpatialIndex;
import dev.hermes.api.world.SpatialIndexRegistration;

import java.util.Collections;
import java.util.List;

/** Test SPI entry registering strategy id {@code testGrid}. */
public final class TestSpatialIndexRegistration implements SpatialIndexRegistration {

    @Override
    public void register(dev.hermes.api.world.SpatialIndexRegistrar registrar) {
        registrar.register("testGrid", cellSize -> new MarkerSpatialIndex());
    }

    static final class MarkerSpatialIndex implements SpatialIndex {
        @Override
        public void rebuild(EntityStore entities) {}

        @Override
        public List<Entity> queryNear(float x, float y, float radius) {
            return Collections.emptyList();
        }

        @Override
        public List<Entity> queryNear(float x, float y, float z, float radius) {
            return Collections.emptyList();
        }

        @Override
        public List<Entity> queryAabb(float minX, float minY, float maxX, float maxY) {
            return Collections.emptyList();
        }
    }
}
