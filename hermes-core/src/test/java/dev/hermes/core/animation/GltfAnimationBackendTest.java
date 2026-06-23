package dev.hermes.core.animation;

import com.badlogic.gdx.graphics.g3d.Model;
import dev.hermes.api.Entity;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.DrawableRig;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GltfAnimationBackendTest {

    private ResourceManagerImpl resources;
    private GltfAnimationBackend backend;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        resources = ResourceManagerImpl.createDefault();
        backend = new GltfAnimationBackend(resources);
    }

    @Test
    void loadsSplitGltfModelResource() {
        ResourceRef ref = ResourceRef.of("models/simple.gltf");
        resources.loadSync(ref, ResourceKind.GLTF_MODEL);
        Model model = ResourceAccess.gltfModel(resources, ref);
        assertInstanceOf(Model.class, model);
        assertTrue(model.animations.size > 0);
    }

    @Test
    void updateAdvancesIdleClipTime() {
        WorldManagerImpl manager = new WorldManagerImpl();
        Entity entity = manager.entities().create("gltf-actor");

        DrawablePart part = DrawablePart.mesh("body", "models/simple.gltf");
        part.setRig(DrawableRig.GLTF);
        manager.entities().addComponent(entity.id(), new Drawables(java.util.List.of(part)));

        AnimationController controller = new AnimationController();
        controller.setRigPart("body");
        controller.setClips(Map.of("idle", AnimationClipRef.gltf("Idle")));
        controller.setDefaultClip("idle");
        controller.setAutoPlay(true);
        controller.initPlayback();
        manager.entities().addComponent(entity.id(), controller);

        backend.bind(entity.id(), controller, controller.activeRef(), resources);
        backend.update(
                entity.id(),
                controller,
                controller.activeRef(),
                0.1f,
                manager.entities(),
                resources);

        assertTrue(controller.timeSeconds() > 0f);
        assertTrue(controller.playing());
    }
}
