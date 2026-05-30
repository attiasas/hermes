package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.core.TestGdx;

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
        int builtinCount = engine.systems().size();
        System global =
                new System() {
                    @Override
                    public void update(WorldManager manager, float deltaSeconds) {
                    }
                };
        System active =
                new System() {
                    @Override
                    public void update(WorldManager manager, float deltaSeconds) {
                    }
                };

        engine.addSystem(global, SystemScope.GLOBAL);
        engine.addSystem(active, SystemScope.ACTIVE_SCENE);

        var entries = engine.systems();
        assertEquals(builtinCount + 2, entries.size());
        assertSame(global, entries.get(entries.size() - 2).system());
        assertEquals(SystemScope.GLOBAL, entries.get(entries.size() - 2).scope());
        assertSame(active, entries.get(entries.size() - 1).system());
        assertEquals(SystemScope.ACTIVE_SCENE, entries.get(entries.size() - 1).scope());
    }

    @Test
    void addSystemDefaultsToGlobalScope() {
        HermesEngineImpl engine = new HermesEngineImpl();
        int builtinCount = engine.systems().size();
        System system =
                new System() {
                    @Override
                    public void update(WorldManager manager, float deltaSeconds) {
                    }
                };

        engine.addSystem(system);

        assertEquals(builtinCount + 1, engine.systems().size());
        HermesEngineImpl.SystemEntry entry = engine.systems().get(engine.systems().size() - 1);
        assertSame(system, entry.system());
        assertEquals(SystemScope.GLOBAL, entry.scope());
    }
}
