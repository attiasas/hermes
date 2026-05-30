package dev.hermes.core.lighting;

import dev.hermes.api.ecs.SceneLightingState;

/** Immutable GPU light slot caps for environment compilation. */
public final class LightingBudgets {

    private final int maxDirectional;
    private final int maxPoint;
    private final int maxSpot;

    public LightingBudgets(int maxDirectional, int maxPoint, int maxSpot) {
        this.maxDirectional = maxDirectional;
        this.maxPoint = maxPoint;
        this.maxSpot = maxSpot;
    }

    public static LightingBudgets defaults() {
        return new LightingBudgets(1, 0, 0);
    }

    public static LightingBudgets from(SceneLightingState state) {
        return new LightingBudgets(state.maxDirectional(), state.maxPoint(), state.maxSpot());
    }

    public int maxDirectional() {
        return maxDirectional;
    }

    public int maxPoint() {
        return maxPoint;
    }

    public int maxSpot() {
        return maxSpot;
    }
}
