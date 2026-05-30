package dev.hermes.core.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.scene.SceneAudioConfig;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoadMetadata;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.ecs.EntityTypeRegistryImpl;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SceneAudioIntegrationTest {

    private WorldManagerImpl manager;
    private ComponentRegistryImpl components;
    private EntityTypeRegistryImpl types;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        types = new EntityTypeRegistryImpl();
        components = new ComponentRegistryImpl();
        BuiltinComponents.register(components);
        manager = new WorldManagerImpl(types, components);
    }

    @Test
    void sceneLoaderParsesAudioBlock() {
        String json =
                "{\n"
                        + "  \"audio\": {\n"
                        + "    \"bgm\": \"overworld\",\n"
                        + "    \"fadeInSeconds\": 1.5,\n"
                        + "    \"fadeOutSeconds\": 0.5,\n"
                        + "    \"pauseBgmOnPause\": true\n"
                        + "  },\n"
                        + "  \"entities\": []\n"
                        + "}\n";

        SceneLoadMetadata metadata =
                SceneLoader.loadFromString("scenes/audio-test.json", json, manager.entities(), components);

        SceneAudioConfig audio = metadata.audioConfig().orElseThrow();
        assertEquals("overworld", audio.bgmPlaylistId().orElseThrow());
        assertEquals(1.5f, audio.fadeInSeconds(), 0.001f);
        assertEquals(0.5f, audio.fadeOutSeconds(), 0.001f);
        assertTrue(audio.pauseBgmOnPause());
    }

    @Test
    void sceneLoaderParsesExplicitBgmPlaylistPath() {
        String json =
                "{\n"
                        + "  \"audio\": { \"bgmPlaylist\": \"custom/bgm.json\" },\n"
                        + "  \"entities\": []\n"
                        + "}\n";

        SceneLoadMetadata metadata =
                SceneLoader.loadFromString("scenes/custom-bgm.json", json, manager.entities(), components);

        assertEquals("custom/bgm.json", metadata.audioConfig().orElseThrow().bgmPlaylistPath().orElseThrow());
    }

    @Test
    void onSceneEnterCrossfadesResolvedPlaylist() {
        RecordingSoundBackend soundBackend = new RecordingSoundBackend();
        RecordingMusicBackend musicBackend = new RecordingMusicBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(soundBackend, musicBackend, new AudioMixerImpl(), new SoundCache(soundBackend));

        SceneAudioConfig config = new SceneAudioConfig("main-menu", null, 2f, 1f, false);
        audio.onSceneEnter("menu", Optional.of(config));

        assertEquals("music/menu.ogg", musicBackend.lastLoadedPath);
    }

    @Test
    void onSceneExitStopsBgmWithFadeOut() {
        RecordingSoundBackend soundBackend = new RecordingSoundBackend();
        RecordingMusicBackend musicBackend = new RecordingMusicBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(soundBackend, musicBackend, new AudioMixerImpl(), new SoundCache(soundBackend));

        SceneAudioConfig config = new SceneAudioConfig("main-menu", null, 1f, 0f, false);
        audio.onSceneEnter("menu", Optional.of(config));
        audio.onSceneExit("menu");

        assertEquals(false, audio.bgm().isPlaying());
    }

    @Test
    void onScenePauseResumesHonorsPauseBgmOnPause() {
        RecordingSoundBackend soundBackend = new RecordingSoundBackend();
        RecordingMusicBackend musicBackend = new RecordingMusicBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(soundBackend, musicBackend, new AudioMixerImpl(), new SoundCache(soundBackend));

        SceneAudioConfig config = new SceneAudioConfig("main-menu", null, 1f, 1f, true);
        audio.onSceneEnter("game", Optional.of(config));
        audio.onScenePause("game");
        assertTrue(musicBackend.paused);
        audio.onSceneResume("game");
        assertTrue(musicBackend.resumed);
    }

    @Test
    void onSceneResumeRestoresBgmAfterOverlayExit() {
        RecordingSoundBackend soundBackend = new RecordingSoundBackend();
        RecordingMusicBackend musicBackend = new RecordingMusicBackend();
        AudioServiceImpl audio =
                new AudioServiceImpl(soundBackend, musicBackend, new AudioMixerImpl(), new SoundCache(soundBackend));

        SceneAudioConfig gameConfig = new SceneAudioConfig("main-menu", null, 1f, 1f, false);
        SceneAudioConfig overlayConfig = new SceneAudioConfig("main-menu", null, 0.5f, 0.5f, false);
        audio.onSceneEnter("game", Optional.of(gameConfig));
        audio.onSceneEnter("overlay", Optional.of(overlayConfig));
        audio.onSceneExit("overlay");
        audio.onSceneResume("game");

        assertTrue(audio.bgm().isPlaying());
    }
}
