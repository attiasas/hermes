package dev.hermes.core.audio;

import dev.hermes.api.audio.AudioBus;
import dev.hermes.api.audio.AudioMixer;
import dev.hermes.api.audio.AudioService;
import dev.hermes.api.audio.BgmController;
import dev.hermes.api.audio.ClipId;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.api.audio.SoundHandle;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneAudioConfig;

import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.core.HermesAssetPaths;

import java.util.Optional;

/** Default {@link AudioService} using libGDX sound playback behind {@link SoundBackend}. */
public final class AudioServiceImpl implements AudioService {

    private static final Logger log = Logs.get(AudioServiceImpl.class);

    private final SoundBackend backend;
    private final SoundCache soundCache;
    private final BgmControllerImpl bgmController;
    private AudioMixer mixer;
    private AudioProfile profile;
    private boolean missingProfileLogged;

    public AudioServiceImpl(SoundBackend backend, AudioMixer mixer, SoundCache soundCache) {
        this(backend, new NoopMusicBackend(), mixer, soundCache);
    }

    AudioServiceImpl(
            SoundBackend backend, MusicBackend musicBackend, AudioMixer mixer, SoundCache soundCache) {
        this.backend = backend;
        this.mixer = mixer;
        this.soundCache = soundCache;
        this.bgmController = new BgmControllerImpl(musicBackend, mixer);
    }

    public static AudioServiceImpl createDefault(AudioMixerImpl mixer) {
        GdxSoundBackend backend = new GdxSoundBackend();
        GdxMusicBackend musicBackend = new GdxMusicBackend();
        return new AudioServiceImpl(backend, musicBackend, mixer, new SoundCache(backend));
    }

    public void setMixer(AudioMixer mixer) {
        this.mixer = mixer;
    }

    public void dispose() {
        soundCache.dispose();
        bgmController.dispose();
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
        return bgmController;
    }

    @Override
    public void loadProfile(String profilePath) {
        if (profilePath == null || profilePath.isBlank()) {
            return;
        }
        if (!HermesAssetPaths.internal(profilePath).exists()) {
            if (!missingProfileLogged) {
                log.warn("Audio profile not found: " + profilePath);
                missingProfileLogged = true;
            }
            return;
        }
        profile = AudioProfileLoader.load(profilePath);
        applyProfileBusVolumes();
    }

    private void applyProfileBusVolumes() {
        for (AudioBus bus : AudioBus.values()) {
            mixer.setVolume(bus, profile.busVolume(bus));
        }
    }

    @Override
    public void tick(float delta, WorldManager activeManager, float surfaceWidth, float surfaceHeight) {
        if (activeManager != null) {
            AudioListenerUpdater.update(backend, activeManager, surfaceWidth, surfaceHeight);
        }
        bgmController.tick(delta);
    }

    @Override
    public void onSceneEnter(String sceneId, Optional<SceneAudioConfig> config) {}

    @Override
    public void onSceneExit(String sceneId) {}

    @Override
    public void onScenePause(String sceneId) {}

    @Override
    public void onSceneResume(String sceneId) {}

    private static final class NoopMusicBackend implements MusicBackend {

        @Override
        public MusicHandle load(String path) {
            return NoopMusicHandle.INSTANCE;
        }

        @Override
        public void disposeMusic() {}
    }

    private static final class NoopMusicHandle implements MusicHandle {

        private static final NoopMusicHandle INSTANCE = new NoopMusicHandle();

        @Override
        public void play(float volume, boolean loop) {}

        @Override
        public void stop() {}

        @Override
        public void setVolume(float volume) {}

        @Override
        public boolean isPlaying() {
            return false;
        }

        @Override
        public void pause() {}

        @Override
        public void resume() {}

        @Override
        public void dispose() {}
    }
}
