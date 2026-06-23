package dev.hermes.core.ecs;

import dev.hermes.api.animation.AnimationClipType;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.ComponentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnimationControllerDeserializerTest {

    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
    }

    @Test
    void deserializesClipsMapAndDefaultClip() {
        String json =
                "{"
                        + "\"clips\":{"
                        + "\"idle\":\"animations/hero-idle.json\","
                        + "\"run\":{\"type\":\"gltf\",\"clip\":\"Run\",\"loop\":true,\"speed\":1.2}"
                        + "},"
                        + "\"rigPart\":\"body\","
                        + "\"default\":\"idle\","
                        + "\"speed\":0.8,"
                        + "\"autoPlay\":true"
                        + "}";

        AnimationController controller =
                (AnimationController)
                        registry.deserialize(
                                "AnimationController",
                                JsonComponentData.parse(json),
                                ComponentContext.EMPTY);

        assertEquals(2, controller.clips().size());
        assertEquals(AnimationClipType.HERMES, controller.clips().get("idle").type());
        assertEquals("animations/hero-idle.json", controller.clips().get("idle").path());
        assertEquals(AnimationClipType.GLTF, controller.clips().get("run").type());
        assertEquals("Run", controller.clips().get("run").clipName());
        assertEquals("idle", controller.defaultClip());
        assertEquals(0.8f, controller.speed());
        assertTrue(controller.autoPlay());
    }

    @Test
    void requiresRigPartWhenAnyClipUsesGltf() {
        String json =
                "{"
                        + "\"clips\":{"
                        + "\"run\":{\"type\":\"gltf\",\"clip\":\"Run\"}"
                        + "}"
                        + "}";

        IllegalArgumentException error =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                registry.deserialize(
                                        "AnimationController",
                                        JsonComponentData.parse(json),
                                        ComponentContext.EMPTY));
        assertTrue(error.getMessage().contains("rigPart"));
    }
}
