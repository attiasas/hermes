package dev.hermes.core.audio;

import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.audio.ClipId;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.FootstepEmitter;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.WorldManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** ACTIVE_SCENE system: plays footstep clips when an entity moves fast enough. */
public final class FootstepSystem implements System {

    private final AudioServiceImpl audio;
    private final Random random = new Random();
    private final Map<EntityId, FootstepState> states = new HashMap<>();

    public FootstepSystem(AudioServiceImpl audio) {
        this.audio = audio;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        if (deltaSeconds <= 0f) {
            return;
        }
        EntityStore entities = manager.entities();
        Set<EntityId> seen = new HashSet<>();

        for (Entity entity : entities.entitiesWith(FootstepEmitter.class)) {
            EntityId entityId = entity.id();
            seen.add(entityId);
            Transform transform = entities.getComponent(entityId, Transform.class);
            FootstepEmitter emitter = entities.getComponent(entityId, FootstepEmitter.class);
            if (transform == null || emitter == null || emitter.clips().length == 0) {
                continue;
            }

            FootstepState state = states.computeIfAbsent(entityId, id -> new FootstepState());
            float dx = transform.x() - state.lastX;
            float dy = transform.y() - state.lastY;
            float dz = transform.z() - state.lastZ;
            float distSq = dx * dx + dy * dy + dz * dz;
            float speed = (float) Math.sqrt(distSq) / deltaSeconds;

            state.lastX = transform.x();
            state.lastY = transform.y();
            state.lastZ = transform.z();
            state.elapsed += deltaSeconds;

            if (speed >= emitter.minSpeed() && state.elapsed >= emitter.intervalSeconds()) {
                playFootstep(emitter, transform);
                state.elapsed = 0f;
            }
        }

        Iterator<Map.Entry<EntityId, FootstepState>> iterator = states.entrySet().iterator();
        while (iterator.hasNext()) {
            if (!seen.contains(iterator.next().getKey())) {
                iterator.remove();
            }
        }
    }

    private void playFootstep(FootstepEmitter emitter, Transform transform) {
        String[] clips = emitter.clips();
        String clip = clips[random.nextInt(clips.length)];
        PlayOptions options =
                PlayOptions.builder()
                        .bus(emitter.bus())
                        .volume(emitter.volume())
                        .worldPosition(transform.x(), transform.y(), transform.z())
                        .build();
        if (emitter.clipIsId()) {
            audio.play(ClipId.of(clip), options);
        } else {
            audio.play(clip, options);
        }
    }

    private static final class FootstepState {
        private float lastX;
        private float lastY;
        private float lastZ;
        private float elapsed;
    }
}
