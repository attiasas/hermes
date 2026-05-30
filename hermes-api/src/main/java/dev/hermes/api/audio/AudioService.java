package dev.hermes.api.audio;

import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneAudioConfig;

import java.util.Optional;

/** Engine audio playback, BGM, and scene lifecycle hooks. */
public interface AudioService {

    AudioMixer mixer();

    /** Play a one-shot or looped SFX; returns handle to adjust or stop. */
    SoundHandle play(String clipPath, PlayOptions options);

    /** Play using a predeclared clip id from {@code audio/profile.json}. */
    SoundHandle play(ClipId clipId, PlayOptions options);

    /** Stop all instances of a clip, or all SFX when {@code clipPath} is null. */
    void stopAll(String clipPath);

    BgmController bgm();

    /** Load profile from assets; no-op if already loaded. Idempotent. */
    void loadProfile(String profilePath);

    /** Update listener and BGM; called by the engine each frame. */
    void tick(float delta, WorldManager activeManager, float surfaceWidth, float surfaceHeight);

    /** Called by {@code SceneStack} on scene enter. */
    void onSceneEnter(String sceneId, Optional<SceneAudioConfig> config);

    /** Called by {@code SceneStack} on scene exit. */
    void onSceneExit(String sceneId);

    /** Called by {@code SceneStack} when a scene is paused under another scene. */
    void onScenePause(String sceneId);

    /** Called by {@code SceneStack} when a paused scene resumes. */
    void onSceneResume(String sceneId);
}
