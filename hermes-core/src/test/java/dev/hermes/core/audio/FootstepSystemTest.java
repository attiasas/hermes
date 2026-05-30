package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.FootstepEmitter;
import dev.hermes.api.ecs.SoundEmitter;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.EntityTypeRegistryImpl;
import dev.hermes.core.ecs.WorldManagerImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class FootstepSystemTest {

    private WorldManagerImpl manager;
    private RecordingSoundBackend backend;
    private SoundEmitterSystem emitterSystem;
    private FootstepSystem footstepSystem;

    @BeforeEach
    void setUp() {
        ComponentRegistryImpl components = new ComponentRegistryImpl();
        BuiltinComponents.register(components);
        manager = new WorldManagerImpl(new EntityTypeRegistryImpl(), components);
        backend = new RecordingSoundBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(backend, new AudioMixerImpl(), new SoundCache(backend));
        audio.loadProfileFromJson(
                "{\"version\":1,\"clips\":{\"footstep\":\"sfx/footstep.wav\"}}");
        emitterSystem = new SoundEmitterSystem(audio);
        footstepSystem = new FootstepSystem(audio);
    }

    @Test
    void soundEmitterSpawnPlaysOnce() {
        Entity entity = manager.entities().create("bell");
        SoundEmitter emitter = new SoundEmitter();
        emitter.setClip("sfx/spawn.wav");
        emitter.setPlayOn(SoundEmitter.PlayOn.SPAWN);
        manager.entities().addComponent(entity.id(), emitter);

        emitterSystem.update(manager, 0.016f);
        assertEquals("sfx/spawn.wav", backend.lastPath);

        backend.lastPath = null;
        emitterSystem.update(manager, 0.016f);
        assertNull(backend.lastPath);
    }

    @Test
    void soundEmitterIntervalPlaysRepeatedly() {
        Entity entity = manager.entities().create("tick");
        SoundEmitter emitter = new SoundEmitter();
        emitter.setClip("sfx/tick.wav");
        emitter.setPlayOn(SoundEmitter.PlayOn.INTERVAL);
        emitter.setIntervalSeconds(0.5f);
        manager.entities().addComponent(entity.id(), emitter);

        emitterSystem.update(manager, 0.25f);
        assertNull(backend.lastPath);

        emitterSystem.update(manager, 0.25f);
        assertEquals("sfx/tick.wav", backend.lastPath);
    }

    @Test
    void footstepPlaysWhenMovingFastEnough() {
        Entity entity = manager.entities().create("walker");
        manager.entities().addComponent(entity.id(), new Transform(0f, 0f, 0f));
        FootstepEmitter footsteps = new FootstepEmitter();
        footsteps.setClips(new String[] {"footstep"});
        footsteps.setClipIsId(true);
        footsteps.setMinSpeed(0.5f);
        footsteps.setIntervalSeconds(0.1f);
        manager.entities().addComponent(entity.id(), footsteps);

        footstepSystem.update(manager, 0.016f);
        assertNull(backend.lastPath);

        Transform transform = manager.entities().getComponent(entity.id(), Transform.class);
        transform.setX(2f);
        footstepSystem.update(manager, 0.5f);
        assertEquals("sfx/footstep.wav", backend.lastPath);
    }
}
