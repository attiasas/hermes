package dev.hermes.core.animation;

import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.resource.ResourceService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** ACTIVE_SCENE system that advances entity animation controllers via backend dispatch. */
public final class AnimationSystem implements System {

    private final AnimationBackendRegistry backends;
    private final ResourceService resources;
    private final Map<EntityId, BindingState> bindings = new HashMap<>();

    public AnimationSystem(AnimationBackendRegistry backends, ResourceService resources) {
        this.backends = Objects.requireNonNull(backends, "backends");
        this.resources = Objects.requireNonNull(resources, "resources");
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        Set<EntityId> seen = new HashSet<>();

        for (Entity entity : entities.entitiesWith(AnimationController.class)) {
            EntityId entityId = entity.id();
            seen.add(entityId);
            AnimationController controller = entities.getComponent(entityId, AnimationController.class);
            if (controller == null) {
                continue;
            }

            if (!controller.playing() && controller.autoPlay() && controller.activeRef() == null) {
                controller.initPlayback();
            }
            if (!controller.playing()) {
                clearBinding(entityId, controller);
                continue;
            }

            AnimationClipRef ref = resolveActiveRef(controller);
            if (ref == null) {
                clearBinding(entityId, controller);
                continue;
            }

            AnimationBackend backend = backends.require(ref.type());
            ensureBinding(entityId, controller, ref, backend);
            backend.update(entityId, controller, ref, deltaSeconds, entities, resources);

            if (backend.isFinished(controller)) {
                backend.unbind(entityId, controller);
                bindings.remove(entityId);
            }
        }

        pruneMissingEntities(seen, entities);
    }

    private AnimationClipRef resolveActiveRef(AnimationController controller) {
        AnimationClipRef current = controller.activeRef();
        if (current != null) {
            return current;
        }
        String clipName = controller.currentClip();
        if (clipName == null || clipName.isBlank()) {
            return null;
        }
        AnimationClipRef resolved = controller.clips().get(clipName);
        controller.setActiveRef(resolved);
        return resolved;
    }

    private void ensureBinding(
            EntityId entityId,
            AnimationController controller,
            AnimationClipRef ref,
            AnimationBackend backend) {
        BindingState previous = bindings.get(entityId);
        String clipName = controller.currentClip();
        if (previous != null
                && previous.type == ref.type()
                && Objects.equals(previous.clipName, clipName)) {
            return;
        }

        if (previous != null) {
            backends.require(previous.type).unbind(entityId, controller);
        }
        backend.bind(entityId, controller, ref, resources);
        bindings.put(entityId, new BindingState(ref.type(), clipName));
    }

    private void clearBinding(EntityId entityId, AnimationController controller) {
        BindingState state = bindings.remove(entityId);
        if (state != null) {
            backends.require(state.type).unbind(entityId, controller);
        }
    }

    private void pruneMissingEntities(Set<EntityId> seen, EntityStore entities) {
        Set<EntityId> remove = new HashSet<>();
        for (Map.Entry<EntityId, BindingState> entry : bindings.entrySet()) {
            EntityId entityId = entry.getKey();
            if (seen.contains(entityId) && entities.getEntity(entityId) != null) {
                continue;
            }
            remove.add(entityId);
        }
        for (EntityId entityId : remove) {
            bindings.remove(entityId);
        }
    }

    private static final class BindingState {
        private final dev.hermes.api.animation.AnimationClipType type;
        private final String clipName;

        private BindingState(dev.hermes.api.animation.AnimationClipType type, String clipName) {
            this.type = type;
            this.clipName = clipName;
        }
    }
}
