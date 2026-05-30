package dev.hermes.api.ecs;

import dev.hermes.api.Component;

import java.util.List;

public final class SceneLightingState implements Component {
    private float[] defaultAmbientColor = {0.4f, 0.4f, 0.4f, 1f};
    private float defaultAmbientIntensity = 1f;
    private float[] defaultDirectionalColor = {0.8f, 0.8f, 0.8f, 1f};
    private float defaultDirectionalIntensity = 1f;
    private float[] defaultDirectionalDirection = {-1f, -0.8f, -0.2f};
    private boolean hasDefaultDirectional = true;

    private List<PointLightSpec> scenePointLights = List.of();
    private List<SpotLightSpec> sceneSpotLights = List.of();

    private int maxDirectional = 1;
    private int maxPoint = 0;
    private int maxSpot = 0;

    private int revision;

    public float[] defaultAmbientColor() {
        return defaultAmbientColor;
    }

    public void setDefaultAmbientColor(float r, float g, float b, float a) {
        defaultAmbientColor[0] = r;
        defaultAmbientColor[1] = g;
        defaultAmbientColor[2] = b;
        defaultAmbientColor[3] = a;
    }

    public float defaultAmbientIntensity() {
        return defaultAmbientIntensity;
    }

    public void setDefaultAmbientIntensity(float defaultAmbientIntensity) {
        this.defaultAmbientIntensity = defaultAmbientIntensity;
    }

    public float[] defaultDirectionalColor() {
        return defaultDirectionalColor;
    }

    public void setDefaultDirectionalColor(float r, float g, float b, float a) {
        defaultDirectionalColor[0] = r;
        defaultDirectionalColor[1] = g;
        defaultDirectionalColor[2] = b;
        defaultDirectionalColor[3] = a;
    }

    public float defaultDirectionalIntensity() {
        return defaultDirectionalIntensity;
    }

    public void setDefaultDirectionalIntensity(float defaultDirectionalIntensity) {
        this.defaultDirectionalIntensity = defaultDirectionalIntensity;
    }

    public float[] defaultDirectionalDirection() {
        return defaultDirectionalDirection;
    }

    public void setDefaultDirectionalDirection(float x, float y, float z) {
        defaultDirectionalDirection[0] = x;
        defaultDirectionalDirection[1] = y;
        defaultDirectionalDirection[2] = z;
    }

    public boolean hasDefaultDirectional() {
        return hasDefaultDirectional;
    }

    public void setHasDefaultDirectional(boolean hasDefaultDirectional) {
        this.hasDefaultDirectional = hasDefaultDirectional;
    }

    public List<PointLightSpec> scenePointLights() {
        return scenePointLights;
    }

    public void setScenePointLights(List<PointLightSpec> scenePointLights) {
        this.scenePointLights = scenePointLights == null ? List.of() : scenePointLights;
    }

    public List<SpotLightSpec> sceneSpotLights() {
        return sceneSpotLights;
    }

    public void setSceneSpotLights(List<SpotLightSpec> sceneSpotLights) {
        this.sceneSpotLights = sceneSpotLights == null ? List.of() : sceneSpotLights;
    }

    public int maxDirectional() {
        return maxDirectional;
    }

    public void setMaxDirectional(int maxDirectional) {
        this.maxDirectional = maxDirectional;
    }

    public int maxPoint() {
        return maxPoint;
    }

    public void setMaxPoint(int maxPoint) {
        this.maxPoint = maxPoint;
    }

    public int maxSpot() {
        return maxSpot;
    }

    public void setMaxSpot(int maxSpot) {
        this.maxSpot = maxSpot;
    }

    public int revision() {
        return revision;
    }

    public void bumpRevision() {
        revision++;
    }
}
