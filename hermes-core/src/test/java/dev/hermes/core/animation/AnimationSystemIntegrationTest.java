package dev.hermes.core.animation;

import dev.hermes.api.Entity;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.resource.ResourceManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnimationSystemIntegrationTest {

    private ResourceManagerImpl resources;
    private AnimationBackendRegistry backends;
    private AnimationSystem system;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        resources = ResourceManagerImpl.createDefault();
        backends = new AnimationBackendRegistry();
        system = new AnimationSystem(backends, resources);
    }

    @Test
    void updateAdvancesHermesClipAndAppliesTransformTrack() {
        backends.register(new HermesTrackBackend());
        WorldManagerImpl manager = new WorldManagerImpl();
        Entity entity = manager.entities().create("actor");
        manager.entities().addComponent(entity.id(), new Transform());

        AnimationController controller = new AnimationController();
        controller.setClips(Map.of("pulse", AnimationClipRef.hermes("animations/scale-x-only.json")));
        controller.setDefaultClip("pulse");
        controller.setAutoPlay(true);
        manager.entities().addComponent(entity.id(), controller);

        system.update(manager, 0.5f);

        Transform transform = manager.entities().getComponent(entity.id(), Transform.class);
        assertEquals(1.5f, transform.scaleX(), 0.0001f);
        assertEquals(0.5f, controller.timeSeconds(), 0.0001f);
        assertTrue(controller.playing());
    }
}
