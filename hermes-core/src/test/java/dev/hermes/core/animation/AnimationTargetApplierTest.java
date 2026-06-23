package dev.hermes.core.animation;

import dev.hermes.api.Entity;
import dev.hermes.api.animation.AnimationTrack;
import dev.hermes.api.animation.Interpolation;
import dev.hermes.api.animation.Keyframe;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.EntityStoreImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AnimationTargetApplierTest {

    private final AnimationTrackEvaluator evaluator = new AnimationTrackEvaluator();
    private final AnimationTargetApplier applier = new AnimationTargetApplier();

    @Test
    void apply_writesTransformXToEntityTransform() {
        EntityStoreImpl store = new EntityStoreImpl();
        Entity entity = store.create("actor");
        store.addComponent(entity.id(), new Transform());
        AnimationTrack track =
                new AnimationTrack(
                        "Transform.x",
                        Interpolation.LINEAR,
                        List.of(new Keyframe(0f, 0f), new Keyframe(1f, 10f)));

        AnimationTrackEvaluator.Value sampled = evaluator.evaluate(track, 0.5f);
        applier.apply(store, entity.id(), track.target(), sampled);

        assertEquals(5f, store.getComponent(entity.id(), Transform.class).x());
    }
}
