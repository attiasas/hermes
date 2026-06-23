package dev.hermes.core.animation;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import dev.hermes.api.EntityId;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/** Shared cache of skinned glTF model instances keyed per entity part. */
public final class RigInstanceCache {

    private static final Map<ResourceManagerImpl, RigInstanceCache> SHARED = new WeakHashMap<>();

    private final Map<Key, RigBinding> bindings = new ConcurrentHashMap<>();

    private RigInstanceCache() {
    }

    public static synchronized RigInstanceCache shared(ResourceManagerImpl resources) {
        Objects.requireNonNull(resources, "resources");
        return SHARED.computeIfAbsent(resources, ignored -> new RigInstanceCache());
    }

    public RigBinding getOrCreate(
            EntityId entityId, String partId, String modelPath, ResourceManagerImpl resources) {
        Objects.requireNonNull(entityId, "entityId");
        Objects.requireNonNull(partId, "partId");
        Objects.requireNonNull(modelPath, "modelPath");
        Objects.requireNonNull(resources, "resources");
        Key key = new Key(entityId, partId);
        return bindings.computeIfAbsent(key, ignored -> createBinding(modelPath, resources));
    }

    public RigBinding find(EntityId entityId, String partId) {
        if (entityId == null || partId == null || partId.isBlank()) {
            return null;
        }
        return bindings.get(new Key(entityId, partId));
    }

    public void remove(EntityId entityId, String partId) {
        if (entityId == null || partId == null || partId.isBlank()) {
            return;
        }
        bindings.remove(new Key(entityId, partId));
    }

    private static RigBinding createBinding(String modelPath, ResourceManagerImpl resources) {
        ResourceRef ref = ResourceRef.of(modelPath);
        resources.loadSync(ref, ResourceKind.GLTF_MODEL);
        ModelInstance instance = new ModelInstance(ResourceAccess.gltfModel(resources, ref));
        return new RigBinding(instance, new com.badlogic.gdx.graphics.g3d.utils.AnimationController(instance));
    }

    public static final class RigBinding {
        private final ModelInstance instance;
        private final com.badlogic.gdx.graphics.g3d.utils.AnimationController animation;
        private String activeClip = "";
        private boolean looping = true;

        private RigBinding(
                ModelInstance instance, com.badlogic.gdx.graphics.g3d.utils.AnimationController animation) {
            this.instance = Objects.requireNonNull(instance, "instance");
            this.animation = Objects.requireNonNull(animation, "animation");
        }

        public ModelInstance instance() {
            return instance;
        }

        public com.badlogic.gdx.graphics.g3d.utils.AnimationController animation() {
            return animation;
        }

        public String activeClip() {
            return activeClip;
        }

        public void setActiveClip(String activeClip) {
            this.activeClip = activeClip == null ? "" : activeClip;
        }

        public boolean looping() {
            return looping;
        }

        public void setLooping(boolean looping) {
            this.looping = looping;
        }
    }

    private static final class Key {
        private final EntityId entityId;
        private final String partId;

        private Key(EntityId entityId, String partId) {
            this.entityId = entityId;
            this.partId = partId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equals(entityId, key.entityId) && Objects.equals(partId, key.partId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityId, partId);
        }
    }
}
