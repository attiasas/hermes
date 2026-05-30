package dev.hermes.core.lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.ecs.DirectionalLight;
import dev.hermes.api.ecs.PointLight;
import dev.hermes.api.ecs.SceneLightingState;

import org.junit.jupiter.api.Test;

final class LightComponentsTest {

    @Test
    void directionalLight_defaults() {
        DirectionalLight light = new DirectionalLight();
        assertTrue(light.enabled());
        assertEquals(1f, light.intensity(), 0.001f);
        assertEquals(4, light.color().length);
    }

    @Test
    void pointLight_rangeDefault() {
        PointLight light = new PointLight();
        assertEquals(10f, light.range(), 0.001f);
    }

    @Test
    void sceneLightingState_engineDefaults() {
        SceneLightingState state = new SceneLightingState();
        assertEquals(0.4f, state.defaultAmbientColor()[0], 0.001f);
        assertTrue(state.hasDefaultDirectional());
        assertEquals(1, state.maxDirectional());
    }
}
