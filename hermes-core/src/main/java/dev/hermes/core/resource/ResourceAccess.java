package dev.hermes.core.resource;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import dev.hermes.api.animation.AnimationClip;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.world.tilemap.TileMapAsset;

/** Typed GPU access for core consumers (render passes, UI, audio). */
public final class ResourceAccess {

    private ResourceAccess() {
    }

    public static TextureRegion textureRegion(ResourceManagerImpl mgr, ResourceRef ref) {
        return require(mgr, ref, ResourceKind.TEXTURE, TextureRegion.class);
    }

    public static Model model(ResourceManagerImpl mgr, ResourceRef ref) {
        return require(mgr, ref, ResourceKind.MODEL, Model.class);
    }

    public static Model gltfModel(ResourceManagerImpl mgr, ResourceRef ref) {
        return require(mgr, ref, ResourceKind.GLTF_MODEL, Model.class);
    }

    public static TextureRegion[] spriteSheetFrames(ResourceManagerImpl mgr, ResourceRef ref) {
        return require(mgr, ref, ResourceKind.SPRITE_SHEET, TextureRegion[].class);
    }

    public static TileMapAsset tileMap(ResourceManagerImpl mgr, ResourceRef ref) {
        return require(mgr, ref, ResourceKind.TILEMAP, TileMapAsset.class);
    }

    public static AnimationClip animationClip(ResourceManagerImpl mgr, ResourceRef ref) {
        return require(mgr, ref, ResourceKind.ANIMATION_CLIP, AnimationClip.class);
    }

    private static <T> T require(
            ResourceManagerImpl mgr, ResourceRef ref, ResourceKind kind, Class<T> type) {
        ResourceKey key = new ResourceKey(ref, kind);
        Object payload = mgr.cache().get(key);
        if (payload == null) {
            throw new ResourceLoadException("Resource not loaded: " + key);
        }
        if (!type.isInstance(payload)) {
            throw new ResourceLoadException(
                    "Resource payload type mismatch for " + key + ": expected "
                            + type.getSimpleName()
                            + ", got "
                            + payload.getClass().getSimpleName());
        }
        return type.cast(payload);
    }
}
