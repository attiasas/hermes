package dev.hermes.api.world;

public class WorldBounds {
    
    private final float minX, minY, minZ, maxX, maxY, maxZ;
    private final boolean unboundedX, unboundedY, unboundedZ;

    public WorldBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.unboundedX = minX == Float.NEGATIVE_INFINITY && maxX == Float.POSITIVE_INFINITY;
        this.unboundedY = minY == Float.NEGATIVE_INFINITY && maxY == Float.POSITIVE_INFINITY;
        this.unboundedZ = minZ == Float.NEGATIVE_INFINITY && maxZ == Float.POSITIVE_INFINITY;
    }

    public float minX() {
        return minX;
    }

    public float minY() {
        return minY;
    }
    
    public float minZ() {
        return minZ;
    }

    public float maxX() {
        return maxX;
    }
    
    public float maxY() {
        return maxY;
    }

    public float maxZ() {
        return maxZ;
    }
    
    public boolean unboundedX() {
        return unboundedX;
    }

    public boolean unboundedY() {
        return unboundedY;
    }
    
    public boolean unboundedZ() {
        return unboundedZ;
    }

    public boolean isUnbounded() {
        return unboundedX && unboundedY && unboundedZ;
    }
    
    public boolean isBounded() {
        return !isUnbounded();
    }

    public static WorldBounds unbounded() {
        return new WorldBounds(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    public static WorldBounds bounded(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return new WorldBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

}
