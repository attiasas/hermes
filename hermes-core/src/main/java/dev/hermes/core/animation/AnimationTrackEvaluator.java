package dev.hermes.core.animation;

import dev.hermes.api.animation.AnimationTrack;
import dev.hermes.api.animation.Interpolation;
import dev.hermes.api.animation.Keyframe;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Samples animation tracks at a given time. */
public final class AnimationTrackEvaluator {

    public Value evaluate(AnimationTrack track, float timeSeconds) {
        Objects.requireNonNull(track, "track");
        List<Keyframe> keyframes = track.keyframes();
        if (keyframes.size() == 1) {
            return Value.of(keyframes.get(0));
        }

        int rightIndex = findFirstAtOrAfter(keyframes, timeSeconds);
        if (rightIndex <= 0) {
            return Value.of(keyframes.get(0));
        }
        if (rightIndex >= keyframes.size()) {
            return Value.of(keyframes.get(keyframes.size() - 1));
        }

        Keyframe left = keyframes.get(rightIndex - 1);
        Keyframe right = keyframes.get(rightIndex);
        if (track.interpolation() == Interpolation.STEP) {
            return Value.of(left);
        }
        return lerp(left, right, timeSeconds);
    }

    private static int findFirstAtOrAfter(List<Keyframe> keyframes, float timeSeconds) {
        int low = 0;
        int high = keyframes.size() - 1;
        int result = keyframes.size();
        while (low <= high) {
            int mid = low + ((high - low) / 2);
            if (keyframes.get(mid).t() >= timeSeconds) {
                result = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return result;
    }

    private static Value lerp(Keyframe left, Keyframe right, float timeSeconds) {
        float span = right.t() - left.t();
        if (span <= 0f) {
            return Value.of(right);
        }
        float alpha = (timeSeconds - left.t()) / span;
        if (left.hasScalarValue() && right.hasScalarValue()) {
            return Value.of(lerp(left.v(), right.v(), alpha));
        }
        if (left.hasArrayValue() && right.hasArrayValue()) {
            float[] a = left.vArray();
            float[] b = right.vArray();
            if (a.length != b.length) {
                throw new IllegalArgumentException("Cannot lerp keyframes with different array lengths");
            }
            float[] out = new float[a.length];
            for (int i = 0; i < a.length; i++) {
                out[i] = lerp(a[i], b[i], alpha);
            }
            return Value.of(out);
        }
        throw new IllegalArgumentException("Cannot lerp scalar and array keyframes together");
    }

    private static float lerp(float a, float b, float alpha) {
        return a + ((b - a) * alpha);
    }

    public static final class Value {
        private final Float scalar;
        private final float[] array;

        private Value(Float scalar, float[] array) {
            this.scalar = scalar;
            this.array = array == null ? null : Arrays.copyOf(array, array.length);
        }

        public static Value of(float scalar) {
            return new Value(scalar, null);
        }

        public static Value of(float[] array) {
            return new Value(null, array);
        }

        public static Value of(Keyframe keyframe) {
            if (keyframe.hasScalarValue()) {
                return of(keyframe.v());
            }
            return of(keyframe.vArray());
        }

        public boolean hasScalar() {
            return scalar != null;
        }

        public float scalar() {
            if (scalar == null) {
                throw new IllegalStateException("Sampled value does not contain a scalar");
            }
            return scalar;
        }

        public float[] array() {
            if (array == null) {
                throw new IllegalStateException("Sampled value does not contain an array");
            }
            return Arrays.copyOf(array, array.length);
        }
    }
}
