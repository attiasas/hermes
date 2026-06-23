package dev.hermes.core.animation;

import dev.hermes.api.EntityId;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.animation.AnimationService;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneManager;
import java.util.Map;
import java.util.Objects;

/** Default {@link AnimationService} backed by {@link AnimationController} components. */
public final class AnimationServiceImpl implements AnimationService {

    private final SceneManager scenes;

    public AnimationServiceImpl(SceneManager scenes) {
        this.scenes = Objects.requireNonNull(scenes, "scenes");
    }

    @Override
    public void play(EntityId entityId, String clipName) {
        play(entityId, clipName, false);
    }

    @Override
    public void play(EntityId entityId, String clipName, boolean restart) {
        AnimationController controller = requireController(entityId);
        String requested = requireClipName(clipName);
        AnimationClipRef ref = requireClipRef(controller.clips(), requested);
        boolean changed = !requested.equals(controller.currentClip());
        if (changed || restart || controller.activeRef() == null) {
            controller.setCurrentClip(requested);
            controller.setActiveRef(ref);
            controller.setTimeSeconds(0f);
        }
        controller.setPlaying(true);
        controller.setFinished(false);
    }

    @Override
    public void stop(EntityId entityId) {
        requireController(entityId).setPlaying(false);
    }

    @Override
    public void setSpeed(EntityId entityId, float speed) {
        requireController(entityId).setSpeed(speed);
    }

    @Override
    public String currentClip(EntityId entityId) {
        return requireController(entityId).currentClip();
    }

    @Override
    public float timeSeconds(EntityId entityId) {
        return requireController(entityId).timeSeconds();
    }

    @Override
    public boolean isPlaying(EntityId entityId) {
        return requireController(entityId).playing();
    }

    @Override
    public boolean isFinished(EntityId entityId) {
        return requireController(entityId).finished();
    }

    private AnimationController requireController(EntityId entityId) {
        Objects.requireNonNull(entityId, "entityId");
        WorldManager activeManager = scenes.activeManager();
        if (activeManager == null) {
            throw new IllegalStateException("AnimationService requires an active scene");
        }
        EntityStore entities = activeManager.entities();
        AnimationController controller = entities.getComponent(entityId, AnimationController.class);
        if (controller == null) {
            throw new IllegalArgumentException("Entity " + entityId + " has no AnimationController component");
        }
        return controller;
    }

    private static String requireClipName(String clipName) {
        if (clipName == null || clipName.isBlank()) {
            throw new IllegalArgumentException("clipName is required");
        }
        return clipName.trim();
    }

    private static AnimationClipRef requireClipRef(Map<String, AnimationClipRef> clips, String clipName) {
        AnimationClipRef ref = clips.get(clipName);
        if (ref == null) {
            throw new IllegalArgumentException("Animation clip not found: " + clipName);
        }
        return ref;
    }
}
