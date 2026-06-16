package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.AmbientSource;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.EntityTypeRegistryImpl;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.ResourceManagerImpl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class AmbientAudioSystemTest {

    private WorldManagerImpl manager;
    private RecordingSoundBackend backend;
    private AmbientAudioSystem system;

    @BeforeAll
    static void initGdx() {
        TestGdx.initHeadlessGl();
    }

    @BeforeEach
    void setUp() {
        ComponentRegistryImpl components = new ComponentRegistryImpl();
        BuiltinComponents.register(components);
        manager = new WorldManagerImpl(new EntityTypeRegistryImpl(), components);
        backend = new RecordingSoundBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(backend, new AudioMixerImpl(), ResourceManagerImpl.createDefault(backend));
        system = new AmbientAudioSystem(audio);
    }

    @Test
    void ambientSourceDeserializes() {
        AmbientSource source = new AmbientSource();
        source.setClip("ambient/wind.ogg");
        source.setMaxDistance(30f);
        assertEquals("ambient/wind.ogg", source.clip());
        assertEquals(30f, source.maxDistance(), 0.001f);
    }

    @Test
    void playsLoopOnFirstSight() {
        Entity entity = manager.entities().create("campfire");
        manager.entities().addComponent(entity.id(), new Transform(3f, 0f, 2f));
        AmbientSource ambient = new AmbientSource();
        ambient.setClip("sfx/test.wav");
        ambient.setLoop(true);
        manager.entities().addComponent(entity.id(), ambient);

        system.update(manager, 0.016f);

        assertEquals("sfx/test.wav", backend.lastPath);
        assertTrue(backend.lastLoop);
    }
}
