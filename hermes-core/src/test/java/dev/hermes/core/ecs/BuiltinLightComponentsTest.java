package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.JsonReader;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.AmbientLight;
import dev.hermes.api.ecs.DirectionalLight;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.PointLight;
import dev.hermes.api.ecs.SpotLight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

final class BuiltinLightComponentsTest {

    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
    }

    @Test
    void deserialize_pointLight_fromJson() {
        PointLight light =
                deserializePointLight(
                        "{ \"color\": [1,0,0,1], \"intensity\": 2, \"range\": 5 }");
        assertEquals(2f, light.intensity(), 0.001f);
        assertEquals(5f, light.range(), 0.001f);
        assertArrayEquals(new float[] {1f, 0f, 0f, 1f}, light.color(), 0.001f);
        assertTrue(light.enabled());
    }

    @Test
    void deserialize_pointLight_defaults() {
        PointLight light = deserializePointLight("{}");
        assertEquals(1f, light.intensity(), 0.001f);
        assertEquals(10f, light.range(), 0.001f);
        assertArrayEquals(new float[] {1f, 1f, 1f, 1f}, light.color(), 0.001f);
        assertTrue(light.enabled());
    }

    @Test
    void deserialize_pointLight_rgbColor() {
        PointLight light = deserializePointLight("{ \"color\": [0.2, 0.4, 0.6] }");
        assertArrayEquals(new float[] {0.2f, 0.4f, 0.6f, 1f}, light.color(), 0.001f);
    }

    @Test
    void deserialize_pointLight_disabled() {
        PointLight light = deserializePointLight("{ \"enabled\": false }");
        assertFalse(light.enabled());
    }

    @Test
    void deserialize_directionalLight_fromJson() {
        DirectionalLight light =
                deserializeDirectionalLight(
                        "{ \"color\": [1,1,0,1], \"intensity\": 0.8, \"direction\": [0, -1, 0] }");
        assertEquals(0.8f, light.intensity(), 0.001f);
        assertArrayEquals(new float[] {1f, 1f, 0f, 1f}, light.color(), 0.001f);
        assertTrue(light.directionOverridden());
        assertArrayEquals(new float[] {0f, -1f, 0f}, light.direction(), 0.001f);
    }

    @Test
    void deserialize_directionalLight_defaults() {
        DirectionalLight light = deserializeDirectionalLight("{}");
        assertEquals(1f, light.intensity(), 0.001f);
        assertFalse(light.directionOverridden());
        assertArrayEquals(new float[] {0f, 0f, -1f}, light.direction(), 0.001f);
    }

    @Test
    void deserialize_spotLight_fromJson() {
        SpotLight light =
                deserializeSpotLight(
                        "{ \"color\": [0,1,0,1], \"intensity\": 3, \"range\": 8, "
                                + "\"cutoffAngle\": 30, \"exponent\": 2, \"direction\": [1, 0, 0] }");
        assertEquals(3f, light.intensity(), 0.001f);
        assertEquals(8f, light.range(), 0.001f);
        assertEquals(30f, light.cutoffAngle(), 0.001f);
        assertEquals(2f, light.exponent(), 0.001f);
        assertArrayEquals(new float[] {0f, 1f, 0f, 1f}, light.color(), 0.001f);
        assertTrue(light.directionOverridden());
        assertArrayEquals(new float[] {1f, 0f, 0f}, light.direction(), 0.001f);
    }

    @Test
    void deserialize_spotLight_defaults() {
        SpotLight light = deserializeSpotLight("{}");
        assertEquals(1f, light.intensity(), 0.001f);
        assertEquals(10f, light.range(), 0.001f);
        assertEquals(45f, light.cutoffAngle(), 0.001f);
        assertEquals(1f, light.exponent(), 0.001f);
        assertFalse(light.directionOverridden());
    }

    @Test
    void deserialize_ambientLight_fromJson() {
        AmbientLight light =
                deserializeAmbientLight("{ \"color\": [0.4, 0.4, 0.4, 1], \"intensity\": 0.5 }");
        assertEquals(0.5f, light.intensity(), 0.001f);
        assertArrayEquals(new float[] {0.4f, 0.4f, 0.4f, 1f}, light.color(), 0.001f);
        assertTrue(light.enabled());
    }

    @Test
    void deserialize_ambientLight_defaults() {
        AmbientLight light = deserializeAmbientLight("{}");
        assertEquals(1f, light.intensity(), 0.001f);
        assertArrayEquals(new float[] {1f, 1f, 1f, 1f}, light.color(), 0.001f);
        assertTrue(light.enabled());
    }

    private PointLight deserializePointLight(String json) {
        return (PointLight)
                registry.deserialize(
                        "scene.json",
                        "lamp",
                        BuiltinComponents.POINT_LIGHT,
                        new JsonComponentData(new JsonReader().parse(json)),
                        emptyContext());
    }

    private DirectionalLight deserializeDirectionalLight(String json) {
        return (DirectionalLight)
                registry.deserialize(
                        "scene.json",
                        "sun",
                        BuiltinComponents.DIRECTIONAL_LIGHT,
                        new JsonComponentData(new JsonReader().parse(json)),
                        emptyContext());
    }

    private SpotLight deserializeSpotLight(String json) {
        return (SpotLight)
                registry.deserialize(
                        "scene.json",
                        "spot",
                        BuiltinComponents.SPOT_LIGHT,
                        new JsonComponentData(new JsonReader().parse(json)),
                        emptyContext());
    }

    private AmbientLight deserializeAmbientLight(String json) {
        return (AmbientLight)
                registry.deserialize(
                        "scene.json",
                        "ambient",
                        BuiltinComponents.AMBIENT_LIGHT,
                        new JsonComponentData(new JsonReader().parse(json)),
                        emptyContext());
    }

    private static ComponentContextImpl emptyContext() {
        return new ComponentContextImpl(new EntityId(1), EntityKind.UNSET, "entity", Map.of());
    }
}
