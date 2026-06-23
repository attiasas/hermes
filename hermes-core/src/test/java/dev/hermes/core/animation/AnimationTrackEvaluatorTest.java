package dev.hermes.core.animation;

import dev.hermes.api.animation.AnimationTrack;
import dev.hermes.api.animation.Interpolation;
import dev.hermes.api.animation.Keyframe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AnimationTrackEvaluatorTest {

    private final AnimationTrackEvaluator evaluator = new AnimationTrackEvaluator();

    @Test
    void evaluateLinearTrack_returnsInterpolatedScalar() {
        AnimationTrack track =
                new AnimationTrack(
                        "Transform.x",
                        Interpolation.LINEAR,
                        List.of(new Keyframe(0f, 0f), new Keyframe(1f, 10f)));

        AnimationTrackEvaluator.Value sampled = evaluator.evaluate(track, 0.5f);

        assertEquals(5f, sampled.scalar());
    }

    @Test
    void evaluateStepTrack_holdsPreviousValue() {
        AnimationTrack track =
                new AnimationTrack(
                        "Transform.x",
                        Interpolation.STEP,
                        List.of(new Keyframe(0f, 1f), new Keyframe(1f, 10f)));

        AnimationTrackEvaluator.Value sampled = evaluator.evaluate(track, 0.5f);

        assertEquals(1f, sampled.scalar());
    }
}
