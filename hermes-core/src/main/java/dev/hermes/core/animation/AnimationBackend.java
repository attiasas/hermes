package dev.hermes.core.animation;

import dev.hermes.api.EntityId;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.animation.AnimationClipType;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.resource.ResourceService;

/** Runtime backend for a specific animation clip format. */
public interface AnimationBackend {

    AnimationClipType type();

    void bind(EntityId entityId, AnimationController controller, AnimationClipRef ref, ResourceService resources);

    void update(
            EntityId entityId,
            AnimationController controller,
            AnimationClipRef ref,
            float deltaSeconds,
            EntityStore entities,
            ResourceService resources);

    void unbind(EntityId entityId, AnimationController controller);

    boolean isFinished(AnimationController controller);
}
