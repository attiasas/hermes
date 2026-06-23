package dev.hermes.api.animation;

import java.util.Arrays;

/** Immutable animation keyframe with scalar or vector value. */
public final class Keyframe {

    private final float t;
    private final Float v;
    private final float[] vArray;

    public Keyframe(float t, float v) {
        this(t, v, null);
    }

    public Keyframe(float t, float[] vArray) {
        this(t, null, vArray);
    }

    private Keyframe(float t, Float v, float[] vArray) {
        this.t = t;
        this.v = v;
        this.vArray = vArray == null ? null : Arrays.copyOf(vArray, vArray.length);
        if ((v == null) == (this.vArray == null)) {
            throw new IllegalArgumentException("Keyframe must have either scalar v or vector vArray");
        }
        if (this.vArray != null && this.vArray.length == 0) {
            throw new IllegalArgumentException("Keyframe vArray must be non-empty");
        }
    }

    public float t() {
        return t;
    }

    public boolean hasScalarValue() {
        return v != null;
    }

    public float v() {
        if (v == null) {
            throw new IllegalStateException("Keyframe does not contain scalar value");
        }
        return v;
    }

    public boolean hasArrayValue() {
        return vArray != null;
    }

    public float[] vArray() {
        return vArray == null ? null : Arrays.copyOf(vArray, vArray.length);
    }
}
