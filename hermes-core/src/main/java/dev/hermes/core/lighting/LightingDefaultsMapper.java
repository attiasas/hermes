package dev.hermes.core.lighting;

import dev.hermes.api.ecs.PointLightSpec;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.ecs.SpotLightSpec;

import java.util.ArrayList;
import java.util.List;

/** Maps parsed scene {@code lighting} JSON into {@link SceneLightingState} fields. */
public final class LightingDefaultsMapper {

    private LightingDefaultsMapper() {}

    public static void apply(SceneLightingBlock spec, SceneLightingState state) {
        spec.ambient().ifPresent(ambient -> {
            applyColor(ambient.color(), state::setDefaultAmbientColor);
            state.setDefaultAmbientIntensity(ambient.intensity());
        });
        spec.directional().ifPresent(directional -> {
            applyColor(directional.color(), state::setDefaultDirectionalColor);
            state.setDefaultDirectionalIntensity(directional.intensity());
            float[] direction = directional.direction();
            if (direction.length >= 3) {
                state.setDefaultDirectionalDirection(direction[0], direction[1], direction[2]);
            }
            state.setHasDefaultDirectional(true);
        });
        List<PointLightSpec> pointLights = new ArrayList<>();
        for (SceneLightingBlock.PointLightEntry entry : spec.pointLights()) {
            pointLights.add(
                    new PointLightSpec(entry.position(), entry.color(), entry.intensity(), entry.range()));
        }
        state.setScenePointLights(pointLights);
        List<SpotLightSpec> spotLights = new ArrayList<>();
        for (SceneLightingBlock.SpotLightEntry entry : spec.spotLights()) {
            spotLights.add(
                    new SpotLightSpec(
                            entry.position(),
                            entry.color(),
                            entry.intensity(),
                            entry.range(),
                            entry.direction(),
                            entry.cutoffAngle(),
                            entry.exponent()));
        }
        state.setSceneSpotLights(spotLights);
    }

    @FunctionalInterface
    private interface ColorSetter {
        void setColor(float r, float g, float b, float a);
    }

    private static void applyColor(float[] color, ColorSetter setter) {
        if (color.length == 3) {
            setter.setColor(color[0], color[1], color[2], 1f);
        } else if (color.length >= 4) {
            setter.setColor(color[0], color[1], color[2], color[3]);
        }
    }
}
