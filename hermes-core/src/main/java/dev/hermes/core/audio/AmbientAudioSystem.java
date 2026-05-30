package dev.hermes.core.audio;

import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.api.audio.SoundHandle;
import dev.hermes.api.ecs.AmbientSource;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.viewport.BackbufferSize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** ACTIVE_SCENE system: plays looping 3D ambient sources tied to entity transforms. */
public final class AmbientAudioSystem implements System {

    private final AudioServiceImpl audio;
    private final Map<EntityId, ActiveAmbient> active = new HashMap<>();

    public AmbientAudioSystem(AudioServiceImpl audio) {
        this.audio = audio;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        Set<EntityId> seen = new HashSet<>();
        ActiveCamera listener =
                CameraResolver.resolve(entities, BackbufferSize.width(), BackbufferSize.height());

        for (Entity entity : entities.entitiesWith(AmbientSource.class)) {
            EntityId entityId = entity.id();
            seen.add(entityId);
            Transform transform = entities.getComponent(entityId, Transform.class);
            AmbientSource source = entities.getComponent(entityId, AmbientSource.class);
            if (transform == null || source == null || source.clip().isBlank()) {
                continue;
            }

            ActiveAmbient state = active.get(entityId);
            if (state == null) {
                PlayOptions options =
                        PlayOptions.builder()
                                .bus(source.bus())
                                .volume(source.volume())
                                .loop(source.loop())
                                .worldPosition(transform.x(), transform.y(), transform.z())
                                .build();
                SoundHandle handle =
                        AudioComponentPlayback.play(audio, source.clip(), source.clipIsId(), options);
                if (handle == null) {
                    continue;
                }
                state = new ActiveAmbient(handle);
                active.put(entityId, state);
            }

            state.handle.setWorldPosition(transform.x(), transform.y(), transform.z());
            float attenuated = attenuateVolume(source, transform, listener);
            state.handle.setVolume(audio.mixer().effectiveGain(source.bus()) * attenuated);
        }

        Iterator<Map.Entry<EntityId, ActiveAmbient>> iterator = active.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EntityId, ActiveAmbient> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
                entry.getValue().handle.stop();
                iterator.remove();
            }
        }
    }

    private static float attenuateVolume(AmbientSource source, Transform transform, ActiveCamera listener) {
        float dx = transform.x() - listener.x();
        float dy = transform.y() - listener.y();
        float dz = transform.z() - listener.z();
        float distSq = dx * dx + dy * dy + dz * dz;
        float minDist = source.minDistance();
        float maxDist = source.maxDistance();
        if (maxDist <= minDist) {
            return source.volume();
        }
        float minDistSq = minDist * minDist;
        float maxDistSq = maxDist * maxDist;
        if (distSq <= minDistSq) {
            return source.volume();
        }
        if (distSq >= maxDistSq) {
            return 0f;
        }
        float dist = (float) Math.sqrt(distSq);
        float t = (dist - minDist) / (maxDist - minDist);
        return source.volume() * (1f - t);
    }

    private static final class ActiveAmbient {
        private final SoundHandle handle;

        ActiveAmbient(SoundHandle handle) {
            this.handle = handle;
        }
    }
}
