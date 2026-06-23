package dev.hermes.core.animation;

import dev.hermes.api.EntityId;
import dev.hermes.api.animation.AnimationClipRef;
import dev.hermes.api.animation.AnimationClipType;
import dev.hermes.api.ecs.AnimationController;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.resource.ResourceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnimationBackendRegistryTest {

    @Test
    void registerAndRequireReturnsBackendByType() {
        AnimationBackendRegistry registry = new AnimationBackendRegistry();
        AnimationBackend backend = new TestBackend(AnimationClipType.HERMES);

        registry.register(backend);

        assertSame(backend, registry.require(AnimationClipType.HERMES));
    }

    @Test
    void requireThrowsWhenBackendTypeIsMissing() {
        AnimationBackendRegistry registry = new AnimationBackendRegistry();

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> registry.require(AnimationClipType.GLTF));

        assertTrue(error.getMessage().contains("GLTF"));
    }

    private static final class TestBackend implements AnimationBackend {
        private final AnimationClipType type;

        private TestBackend(AnimationClipType type) {
            this.type = type;
        }

        @Override
        public AnimationClipType type() {
            return type;
        }

        @Override
        public void bind(EntityId entityId, AnimationController controller, AnimationClipRef ref, ResourceService resources) {
        }

        @Override
        public void update(
                EntityId entityId,
                AnimationController controller,
                AnimationClipRef ref,
                float deltaSeconds,
                EntityStore entities,
                ResourceService resources) {
        }

        @Override
        public void unbind(EntityId entityId, AnimationController controller) {
        }

        @Override
        public boolean isFinished(AnimationController controller) {
            return false;
        }
    }
}
