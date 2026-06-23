package dev.hermes.core.animation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneManager;
import dev.hermes.api.scene.SceneRegistry;
import dev.hermes.api.scene.SceneStackPolicy;
import dev.hermes.core.ecs.WorldManagerImpl;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class AnimationServiceImplTest {

    @Test
    void playSwitchesCurrentClipAndResetsTime() {
        WorldManagerImpl world = new WorldManagerImpl();
        Entity entity = world.entities().create("actor");
        AnimationController controller = new AnimationController();
        AnimationClipRef idle = AnimationClipRef.hermes("animations/test-clip.json");
        AnimationClipRef run = AnimationClipRef.hermes("animations/scale-x-only.json");
        controller.setClips(Map.of("idle", idle, "run", run));
        controller.setCurrentClip("idle");
        controller.setActiveRef(idle);
        controller.setTimeSeconds(2.5f);
        controller.setPlaying(true);
        controller.setFinished(true);
        world.entities().addComponent(entity.id(), controller);

        AnimationServiceImpl service = new AnimationServiceImpl(new StaticSceneManager(world));

        service.play(entity.id(), "run");

        assertEquals("run", controller.currentClip());
        assertNotNull(controller.activeRef());
        assertEquals(run.path(), controller.activeRef().path());
        assertEquals(0f, controller.timeSeconds(), 0.0001f);
        assertTrue(controller.playing());
        assertFalse(controller.finished());
    }

    private static final class StaticSceneManager implements SceneManager {
        private final WorldManagerImpl world;

        private StaticSceneManager(WorldManagerImpl world) {
            this.world = world;
        }

        @Override
        public void request(dev.hermes.api.scene.SceneChangeRequest request) {}

        @Override
        public void processPending() {}

        @Override
        public dev.hermes.api.ecs.WorldManager activeManager() {
            return world;
        }

        @Override
        public SceneHandle active() {
            return null;
        }

        @Override
        public List<SceneHandle> visibleScenes() {
            return List.of();
        }

        @Override
        public List<SceneHandle> updateScenes() {
            return List.of();
        }

        @Override
        public SceneRegistry registry() {
            throw new UnsupportedOperationException("unused in test");
        }

        @Override
        public int stackDepth() {
            return 1;
        }

        @Override
        public void setStackPolicy(SceneStackPolicy policy) {}

        @Override
        public SceneStackPolicy stackPolicy() {
            return SceneStackPolicy.defaults();
        }
    }
}
