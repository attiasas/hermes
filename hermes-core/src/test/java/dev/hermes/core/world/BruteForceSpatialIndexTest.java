package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import java.util.List;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.SpatialPresence;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.WorldManagerImpl;

public class BruteForceSpatialIndexTest {

    private static void assertNames(List<Entity> entities, String... expected) {
        assertEquals(expected.length, entities.size());
    
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], entities.get(i).name());
        }
    }

    @Test
    void queryNear2dReturnsOnlyEntitiesInsideRadius() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        Entity a = es.create("a");
        es.addComponent(a.id(), new Transform(0f, 0f));

        Entity b = es.create("b");
        es.addComponent(b.id(), new Transform(3f, 4f)); // distance = 5

        Entity c = es.create("c");
        es.addComponent(c.id(), new Transform(6f, 8f)); // distance = 10

        manager.space().spatial().rebuild(es);

        assertNames(manager.space().queryNear(0f, 0f, 5f),"a", "b");

        assertNames(manager.space().queryNear(0f, 0f, 4.999f),"a");

        assertNames(manager.space().queryNear(0f, 0f, 10f),"a", "b", "c");
    }

    @Test
    void queryNear2dWithZeroRadiusReturnsExactMatchesOnly() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        Entity a = es.create("a");
        es.addComponent(a.id(), new Transform(10f, 10f));

        Entity b = es.create("b");
        es.addComponent(b.id(), new Transform(10.001f, 10f));

        manager.space().spatial().rebuild(es);

        assertNames(manager.space().queryNear(10f, 10f, 0f),"a");
    }

    @Test
    void queryNearOnEmptyIndexReturnsEmptyList() {
        WorldManagerImpl manager = new WorldManagerImpl();
    
        manager.space().spatial().rebuild(manager.entities());
    
        assertEquals(0, manager.space().queryNear(0f, 0f, 100f).size());
    }

    @Test
    void queryNearIgnoresEntitiesWithoutTransform() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        es.create("no-transform");

        manager.space().spatial().rebuild(es);

        assertEquals(0, manager.space().queryNear(0f, 0f, 100f).size());
    }


    @Test
    void queryNear3dReturnsEntitiesInsideSphere() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        Entity a = es.create("a");
        es.addComponent(a.id(), new Transform(0f, 0f, 0f));

        Entity b = es.create("b");
        es.addComponent(b.id(), new Transform(1f, 2f, 2f)); // distance = 3

        Entity c = es.create("c");
        es.addComponent(c.id(), new Transform(10f, 0f, 0f));

        manager.space().spatial().rebuild(es);

        assertNames(manager.space().queryNear(0f, 0f, 0f, 3f),"a", "b");

        assertNames(manager.space().queryNear(0f, 0f, 0f, 2.999f), "a");
    }

    @Test
    void queryAabbIncludesPointsInsideOrOnBoundary() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        Entity a = es.create("a");
        es.addComponent(a.id(), new Transform(10f, 10f));
        es.addComponent(a.id(), new SpatialPresence());

        Entity b = es.create("b");
        es.addComponent(b.id(), new Transform(20f, 20f));
        es.addComponent(b.id(), new SpatialPresence());

        Entity c = es.create("c");
        es.addComponent(c.id(), new Transform(30f, 30f));
        es.addComponent(c.id(), new SpatialPresence());

        manager.space().spatial().rebuild(es);

        assertNames(manager.space().queryAabb(10f, 10f, 20f, 20f),"a", "b");
    }

    @Test
    void queryAabbIncludesEntitiesWhoseRadiusIntersectsBox() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        SpatialPresence sp = new SpatialPresence();
        sp.setRadius(5f);

        Entity e = es.create("e");
        es.addComponent(e.id(), new Transform(8f, 8f));
        es.addComponent(e.id(), sp);

        manager.space().spatial().rebuild(es);

        // Even though center (8,8) is outside, its bounds intersect.
        assertNames(manager.space().queryAabb(10f, 10f, 20f, 20f),"e");
    }


    @Test
    void queryAabbUsesHalfWidthAndHalfHeight() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        SpatialPresence sp = new SpatialPresence();
        sp.setHalfWidth(10f);
        sp.setHalfHeight(5f);

        Entity e = es.create("e");
        es.addComponent(e.id(), new Transform(0f, 0f));
        es.addComponent(e.id(), sp);

        manager.space().spatial().rebuild(es);

        // This specifically verifies that rectangular bounds are being used instead of radius.
        assertNames(manager.space().queryAabb(9f, -1f, 11f, 1f),"e");
    }

    @Test
    void rebuildReflectsNewEntities() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        Entity a = es.create("a");
        es.addComponent(a.id(), new Transform(0f, 0f));

        manager.space().spatial().rebuild(es);

        assertNames(manager.space().queryNear(0f, 0f, 1f), "a");

        Entity b = es.create("b");
        es.addComponent(b.id(), new Transform(0f, 0f));

        manager.space().spatial().rebuild(es);

        assertNames(manager.space().queryNear(0f, 0f, 1f), "a", "b");
    }

    @Test
    void entitiesAddedAfterRebuildAreNotVisibleUntilRebuild() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        manager.space().spatial().rebuild(es);

        Entity e = es.create("e");
        es.addComponent(e.id(), new Transform(0f, 0f));

        assertEquals(
            0,
            manager.space().queryNear(0f, 0f, 10f).size()
        );
    }

    @Test
    void queryAabbIgnoresEntitiesWithoutSpatialPresence() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        Entity e = es.create("e");
        es.addComponent(e.id(), new Transform(0f, 0f));

        manager.space().spatial().rebuild(es);

        assertEquals(
            0,
            manager.space().queryAabb(-10f, -10f, 10f, 10f).size()
        );
    }

    @Test
    void queryAabbPrefersRectangleBoundsOverRadius() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        SpatialPresence sp = new SpatialPresence();
        sp.setRadius(100f);
        sp.setHalfWidth(1f);
        sp.setHalfHeight(1f);

        Entity e = es.create("e");
        es.addComponent(e.id(), new Transform(50f, 50f));
        es.addComponent(e.id(), sp);

        manager.space().spatial().rebuild(es);

        assertEquals(
            0,
            manager.space().queryAabb(-10f, -10f, 10f, 10f).size()
        );
    }

    @Test
    void queryAabbExcludesRadiusBoundsOutsideBox() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        SpatialPresence sp = new SpatialPresence();
        sp.setRadius(2f);

        Entity e = es.create("e");
        es.addComponent(e.id(), new Transform(0f, 0f));
        es.addComponent(e.id(), sp);

        manager.space().spatial().rebuild(es);

        assertEquals(
            0,
            manager.space().queryAabb(10f, 10f, 20f, 20f).size()
        );
    }

    @Test
    void queryNearHandlesNegativeCoordinates() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        Entity e = es.create("e");
        es.addComponent(e.id(), new Transform(-3f, -4f));

        manager.space().spatial().rebuild(es);

        assertNames(
            manager.space().queryNear(0f, 0f, 5f),
            "e"
        );
    }

    @Test
    void queryAabbTreatsTouchingEdgesAsIntersection() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore es = manager.entities();

        SpatialPresence sp = new SpatialPresence();
        sp.setHalfWidth(5f);
        sp.setHalfHeight(5f);

        Entity e = es.create("e");
        es.addComponent(e.id(), new Transform(5f, 5f));
        es.addComponent(e.id(), sp);

        manager.space().spatial().rebuild(es);

        assertNames(
            manager.space().queryAabb(10f, 0f, 20f, 20f),
            "e"
        );
    }
}
