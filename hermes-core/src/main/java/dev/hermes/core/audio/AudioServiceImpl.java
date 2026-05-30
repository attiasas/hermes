package dev.hermes.core.audio;

import dev.hermes.api.audio.AudioMixer;
import dev.hermes.api.audio.AudioService;
import dev.hermes.api.audio.BgmController;
import dev.hermes.api.audio.ClipId;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.api.audio.SoundHandle;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneAudioConfig;

import java.util.Optional;

/** Default {@link AudioService} using libGDX sound playback behind {@link SoundBackend}. */
public final class AudioServiceImpl implements AudioService {

    private final SoundBackend backend;
    private final SoundCache soundCache;
    private AudioMixer mixer;
    private AudioProfile profile;

    public AudioServiceImpl(SoundBackend backend, AudioMixer mixer, SoundCache soundCache) {
        this.backend = backend;
        this.mixer = mixer;
        this.soundCache = soundCache;
    }

    public static AudioServiceImpl createDefault(AudioMixerImpl mixer) {
        GdxSoundBackend backend = new GdxSoundBackend();
        return new AudioServiceImpl(backend, mixer, new SoundCache(backend));
    }

    public void setMixer(AudioMixer mixer) {
        this.mixer = mixer;
    }

    public void dispose() {
        soundCache.dispose();
    }

    @Override
    public AudioMixer mixer() {
        return mixer;
    }

    @Override
    public SoundHandle play(String clipPath, PlayOptions options) {
        soundCache.soundForPath(clipPath);
        float gain = mixer.effectiveGain(options.bus()) * options.volume();
        long id = backend.play(clipPath, gain, options.pitch(), options.pan(), options.loop());
        if (options.worldX().isPresent()) {
            backend.setPosition(
                    clipPath,
                    id,
                    options.worldX().get(),
                    options.worldY().orElse(0f),
                    options.worldZ().orElse(0f));
        }
        return new GdxSoundHandle(backend, clipPath, id, gain);
    }

    @Override
    public SoundHandle play(ClipId clipId, PlayOptions options) {
        if (profile != null) {
            Optional<String> path = profile.resolveClip(clipId.id());
            if (path.isPresent()) {
                return play(path.get(), options);
            }
        }
        return play(clipId.id(), options);
    }

    @Override
    public void stopAll(String clipPath) {}

    @Override
    public BgmController bgm() {
        return NoopBgmController.INSTANCE;
    }

    @Override
    public void loadProfile(String profilePath) {}

    @Override
    public void tick(float delta, WorldManager activeManager, float surfaceWidth, float surfaceHeight) {}

    @Override
    public void onSceneEnter(String sceneId, Optional<SceneAudioConfig> config) {}

    @Override
    public void onSceneExit(String sceneId) {}

    @Override
    public void onScenePause(String sceneId) {}

    @Override
    public void onSceneResume(String sceneId) {}

    private static final class NoopBgmController implements BgmController {

        private static final NoopBgmController INSTANCE = new NoopBgmController();

        @Override
        public void playPlaylist(String playlistAssetPath) {}

        @Override
        public void playRandom(String playlistAssetPath) {}

        @Override
        public void crossfadeTo(String playlistAssetPath, float fadeSeconds) {}

        @Override
        public void stop(float fadeSeconds) {}

        @Override
        public void pause() {}

        @Override
        public void resume() {}

        @Override
        public void setVolume(float volume01) {}

        @Override
        public boolean isPlaying() {
            return false;
        }
    }
}
