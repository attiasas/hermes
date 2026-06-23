package dev.hermes.api.animation;

import dev.hermes.api.EntityId;

/** Runtime controls for entity animation playback on the active scene. */
public interface AnimationService {

    void play(EntityId entityId, String clipName);

    void play(EntityId entityId, String clipName, boolean restart);

    void stop(EntityId entityId);

    void setSpeed(EntityId entityId, float speed);

    String currentClip(EntityId entityId);

    float timeSeconds(EntityId entityId);

    boolean isPlaying(EntityId entityId);

    boolean isFinished(EntityId entityId);
}
