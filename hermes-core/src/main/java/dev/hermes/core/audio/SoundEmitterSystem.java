package dev.hermes.core.audio;

import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.api.audio.SoundHandle;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.SoundEmitter;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** ACTIVE_SCENE system: auto-plays {@link SoundEmitter} clips on spawn or interval. */
public final class SoundEmitterSystem implements System {

    private final AudioServiceImpl audio;
    private final Map<EntityId, EmitterState> states = new HashMap<>();

    public SoundEmitterSystem(AudioServiceImpl audio) {
        this.audio = audio;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        Set<EntityId> seen = new HashSet<>();

        for (Entity entity : entities.entitiesWith(SoundEmitter.class)) {
            EntityId entityId = entity.id();
            seen.add(entityId);
            SoundEmitter emitter = entities.getComponent(entityId, SoundEmitter.class);
            if (emitter == null || emitter.clip().isBlank()) {
                continue;
            }

            EmitterState state = states.computeIfAbsent(entityId, id -> new EmitterState());
            if (emitter.playOn() == SoundEmitter.PlayOn.SPAWN && !state.spawnPlayed) {
                play(emitter, state);
                state.spawnPlayed = true;
            } else if (emitter.playOn() == SoundEmitter.PlayOn.INTERVAL) {
                state.elapsed += deltaSeconds;
                if (state.elapsed >= emitter.intervalSeconds()) {
                    play(emitter, state);
                    state.elapsed = 0f;
                }
            }
        }

        Iterator<Map.Entry<EntityId, EmitterState>> iterator = states.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EntityId, EmitterState> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
                stopLooping(entry.getValue());
                iterator.remove();
            }
        }
    }

    private void play(SoundEmitter emitter, EmitterState state) {
        PlayOptions options =
                PlayOptions.builder()
                        .bus(emitter.bus())
                        .volume(emitter.volume())
                        .pitch(emitter.pitch())
                        .loop(emitter.loop())
                        .build();
        SoundHandle handle =
                AudioComponentPlayback.play(audio, emitter.clip(), emitter.clipIsId(), options);
        if (emitter.loop()) {
            state.loopHandles.add(handle);
        }
    }

    private static void stopLooping(EmitterState state) {
        for (SoundHandle handle : state.loopHandles) {
            handle.stop();
        }
        state.loopHandles.clear();
    }

    private static final class EmitterState {
        private boolean spawnPlayed;
        private float elapsed;
        private final List<SoundHandle> loopHandles = new ArrayList<>();
    }
}
