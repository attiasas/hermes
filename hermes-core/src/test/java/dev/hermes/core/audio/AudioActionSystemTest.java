package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.input.InputActions;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.EntityTypeRegistryImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class AudioActionSystemTest {

    private RecordingSoundBackend backend;
    private FakeInputActions actions;
    private AudioActionSystem system;

    @BeforeEach
    void setUp() {
        backend = new RecordingSoundBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(backend, new AudioMixerImpl(), new SoundCache(backend));
        audio.loadProfileFromJson(
                "{\"version\":1,\"clips\":{\"c\":\"sfx/c.wav\"},"
                        + "\"actionSounds\":{\"ui.click\":\"c\"}}");
        actions = new FakeInputActions();
        system = new AudioActionSystem(actions, audio);
    }

    @Test
    void playsClipOnJustPressedAction() {
        actions.pressJust("ui.click");
        system.update(new WorldManagerImpl(new EntityTypeRegistryImpl(), new ComponentRegistryImpl()), 0.016f);
        assertEquals("sfx/c.wav", backend.lastPath);
    }

    private static final class FakeInputActions implements InputActions {

        private final java.util.Map<String, Boolean> justPressed = new java.util.HashMap<>();

        void pressJust(String action) {
            justPressed.put(action, true);
        }

        @Override
        public boolean justPressed(String action) {
            return justPressed.getOrDefault(action, false);
        }

        @Override
        public boolean pressed(String action) {
            return justPressed(action);
        }

        @Override
        public boolean justReleased(String action) {
            return false;
        }

        @Override
        public float axis(String action) {
            return 0f;
        }

        @Override
        public void axis2(String action, float[] out) {
            out[0] = 0f;
            out[1] = 0f;
        }

        @Override
        public String context() {
            return "";
        }
    }
}
