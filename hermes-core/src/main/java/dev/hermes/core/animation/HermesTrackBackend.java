package dev.hermes.core.animation;

import dev.hermes.api.EntityId;
import dev.hermes.api.animation.AnimationClip;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.animation.AnimationClipType;
import dev.hermes.api.animation.AnimationTrack;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.api.resource.ResourceService;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;

import java.util.Objects;

/** Backend that evaluates and applies Hermes JSON animation tracks. */
public final class HermesTrackBackend implements AnimationBackend {

    private final AnimationTrackEvaluator evaluator;
    private final AnimationTargetApplier applier;

    public HermesTrackBackend() {
        this(new AnimationTrackEvaluator(), new AnimationTargetApplier());
    }

    public HermesTrackBackend(AnimationTrackEvaluator evaluator, AnimationTargetApplier applier) {
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
        this.applier = Objects.requireNonNull(applier, "applier");
    }

    @Override
    public AnimationClipType type() {
        return AnimationClipType.HERMES;
    }

    @Override
    public void bind(EntityId entityId, AnimationController controller, AnimationClipRef ref, ResourceService resources) {
        // Hermes track playback has no per-entity bind resources at this stage.
    }

    @Override
    public void update(
            EntityId entityId,
            AnimationController controller,
            AnimationClipRef ref,
            float deltaSeconds,
            EntityStore entities,
            ResourceService resources) {
        Objects.requireNonNull(entityId, "entityId");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(ref, "ref");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(resources, "resources");

        AnimationClip clip = loadClip(resources, ref);
        float effectiveDelta = deltaSeconds * controller.speed() * ref.speed();
        float sampleTime = advanceTime(controller, ref, clip, effectiveDelta);

        for (AnimationTrack track : clip.tracks()) {
            AnimationTrackEvaluator.Value sampled = evaluator.evaluate(track, sampleTime);
            applier.apply(entities, entityId, track.target(), sampled);
        }
    }

    @Override
    public void unbind(EntityId entityId, AnimationController controller) {
        // Hermes track playback has no per-entity bound state to release.
    }

    @Override
    public boolean isFinished(AnimationController controller) {
        return controller.finished();
    }

    private static AnimationClip loadClip(ResourceService resources, AnimationClipRef ref) {
        if (!(resources instanceof ResourceManagerImpl)) {
            throw new IllegalArgumentException("HermesTrackBackend requires ResourceManagerImpl");
        }
        ResourceRef clipRef = resources.resolve(ref.path());
        resources.loadSync(clipRef, ResourceKind.ANIMATION_CLIP);
        return ResourceAccess.animationClip((ResourceManagerImpl) resources, clipRef);
    }

    private static float advanceTime(
            AnimationController controller, AnimationClipRef ref, AnimationClip clip, float effectiveDelta) {
        float duration = clip.duration();
        float next = controller.timeSeconds() + effectiveDelta;
        boolean looping = clip.loop() && ref.loop();

        if (duration <= 0f) {
            controller.setTimeSeconds(0f);
            controller.setFinished(false);
            return 0f;
        }

        if (next >= duration) {
            if (looping) {
                next = next % duration;
                controller.setFinished(false);
            } else {
                next = duration;
                controller.setPlaying(false);
                controller.setFinished(true);
            }
        } else if (next < 0f) {
            next = 0f;
            controller.setFinished(false);
        } else {
            controller.setFinished(false);
        }

        controller.setTimeSeconds(next);
        return next;
    }
}
