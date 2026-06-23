package dev.hermes.core.animation;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import dev.hermes.api.EntityId;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.animation.AnimationClipType;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.DrawableRig;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.resource.ResourceService;
import dev.hermes.core.resource.ResourceManagerImpl;

import java.util.Objects;

/** Backend that advances glTF skeletal clips on cached per-entity model instances. */
public final class GltfAnimationBackend implements AnimationBackend {

    private final RigInstanceCache rigInstances;

    public GltfAnimationBackend(ResourceManagerImpl resources) {
        this(RigInstanceCache.shared(resources));
    }

    public GltfAnimationBackend(RigInstanceCache rigInstances) {
        this.rigInstances = Objects.requireNonNull(rigInstances, "rigInstances");
    }

    @Override
    public AnimationClipType type() {
        return AnimationClipType.GLTF;
    }

    @Override
    public void bind(EntityId entityId, AnimationController controller, AnimationClipRef ref, ResourceService resources) {
        // Requires EntityStore access to resolve rig model path; binding happens lazily in update().
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
        ResourceManagerImpl manager = requireManager(resources);

        DrawablePart rigPart = resolveRigPart(entities, entityId, controller.rigPart());
        if (rigPart == null || rigPart.model() == null || rigPart.model().isBlank()) {
            controller.setPlaying(false);
            controller.setFinished(true);
            return;
        }

        RigInstanceCache.RigBinding binding =
                rigInstances.getOrCreate(entityId, rigPart.id(), rigPart.model(), manager);
        ensureClip(boundClipName(ref), ref.loop(), binding);

        Animation clip = requireClip(binding, ref.clipName());
        float effectiveDelta = deltaSeconds * controller.speed() * ref.speed();
        float sampleTime = advanceTime(controller, ref, clip.duration, effectiveDelta);

        if (binding.animation().current != null) {
            binding.animation().current.time = sampleTime;
            binding.animation().update(0f);
        }
    }

    @Override
    public void unbind(EntityId entityId, AnimationController controller) {
        if (entityId == null || controller == null || controller.rigPart() == null || controller.rigPart().isBlank()) {
            return;
        }
        rigInstances.remove(entityId, controller.rigPart());
    }

    @Override
    public boolean isFinished(AnimationController controller) {
        return controller.finished();
    }

    private static ResourceManagerImpl requireManager(ResourceService resources) {
        if (!(resources instanceof ResourceManagerImpl)) {
            throw new IllegalArgumentException("GltfAnimationBackend requires ResourceManagerImpl");
        }
        return (ResourceManagerImpl) resources;
    }

    private static DrawablePart resolveRigPart(EntityStore entities, EntityId entityId, String rigPartId) {
        if (rigPartId == null || rigPartId.isBlank()) {
            return null;
        }
        Drawables drawables = entities.getComponent(entityId, Drawables.class);
        if (drawables == null) {
            return null;
        }
        for (DrawablePart part : drawables.parts()) {
            if (!rigPartId.equals(part.id())) {
                continue;
            }
            if (part.rig() != DrawableRig.GLTF) {
                return null;
            }
            return part;
        }
        return null;
    }

    private static void ensureClip(String clipName, boolean loop, RigInstanceCache.RigBinding binding) {
        if (clipName.equals(binding.activeClip()) && loop == binding.looping()) {
            return;
        }
        binding.animation().setAnimation(clipName, loop ? -1 : 1, null);
        binding.setActiveClip(clipName);
        binding.setLooping(loop);
    }

    private static Animation requireClip(RigInstanceCache.RigBinding binding, String clipName) {
        Animation clip = binding.instance().getAnimation(clipName);
        if (clip == null) {
            throw new IllegalArgumentException("glTF clip not found: " + clipName);
        }
        return clip;
    }

    private static String boundClipName(AnimationClipRef ref) {
        String clipName = ref.clipName();
        if (clipName == null || clipName.isBlank()) {
            throw new IllegalArgumentException("AnimationClipRef.gltf clipName is required");
        }
        return clipName;
    }

    private static float advanceTime(
            AnimationController controller, AnimationClipRef ref, float duration, float effectiveDelta) {
        float next = controller.timeSeconds() + effectiveDelta;
        boolean looping = ref.loop();

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
