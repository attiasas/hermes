package dev.hermes.api.math;

public final class ScreenRay {

    public final float originX;
    public final float originY;
    public final float originZ;
    public final float directionX;
    public final float directionY;
    public final float directionZ;

    public ScreenRay(
            float originX,
            float originY,
            float originZ,
            float directionX,
            float directionY,
            float directionZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.directionX = directionX;
        this.directionY = directionY;
        this.directionZ = directionZ;
    }
}
