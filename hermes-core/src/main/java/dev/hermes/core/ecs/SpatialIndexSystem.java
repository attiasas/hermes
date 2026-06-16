package dev.hermes.core.ecs;

import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;

/** Rebuilds the scene spatial index each frame (v1 brute-force friendly). */
public final class SpatialIndexSystem implements System {

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        manager.space().spatial().rebuild(manager.entities());
    }
}
