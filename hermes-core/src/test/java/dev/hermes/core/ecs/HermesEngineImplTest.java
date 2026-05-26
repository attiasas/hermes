package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.ecs.World;
import dev.hermes.core.TestGdx;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class HermesEngineImplTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void systemsReturnsEntriesWithScope() {
        HermesEngineImpl engine = new HermesEngineImpl();
        System global =
                new System() {
                    @Override
                    public void update(World world, float deltaSeconds) {
                    }
                };
        System active =
                new System() {
                    @Override
                    public void update(World world, float deltaSeconds) {
                    }
                };

        engine.addSystem(global, SystemScope.GLOBAL);
        engine.addSystem(active, SystemScope.ACTIVE_SCENE);

        List<HermesEngineImpl.SystemEntry> entries = new ArrayList<>(engine.systems());
        assertEquals(3, entries.size());
        assertEquals(SystemScope.GLOBAL, entries.get(0).scope());
        assertSame(global, entries.get(1).system());
        assertEquals(SystemScope.GLOBAL, entries.get(1).scope());
        assertSame(active, entries.get(2).system());
        assertEquals(SystemScope.ACTIVE_SCENE, entries.get(2).scope());
    }

    @Test
    void addSystemDefaultsToGlobalScope() {
        HermesEngineImpl engine = new HermesEngineImpl();
        System system = new System() {
            @Override
            public void update(World world, float deltaSeconds) {
            }
        };

        engine.addSystem(system);

        assertEquals(2, engine.systems().size());
        HermesEngineImpl.SystemEntry entry = engine.systems().get(1);
        assertSame(system, entry.system());
        assertEquals(SystemScope.GLOBAL, entry.scope());
    }
}
