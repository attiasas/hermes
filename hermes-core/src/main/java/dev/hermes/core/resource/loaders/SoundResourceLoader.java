package dev.hermes.core.resource.loaders;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.audio.SoundBackend;
import dev.hermes.core.resource.DecodedPayload;
import dev.hermes.core.resource.ResourceLoader;
import dev.hermes.core.resource.ResourcePlatform;

import java.util.Objects;

/** Loads sound clips through an injectable {@link SoundBackend}. */
public final class SoundResourceLoader implements ResourceLoader {

    /** Cached payload when sound loading is skipped on HTML. */
    public static final Object SKIPPED = new Object();

    private final SoundBackend backend;

    public SoundResourceLoader(SoundBackend backend) {
        this.backend = Objects.requireNonNull(backend, "backend");
    }

    @Override
    public ResourceKind kind() {
        return ResourceKind.SOUND;
    }

    @Override
    public DecodedPayload decode(String path) {
        if (ResourcePlatform.isHtmlPlatform()) {
            return DecodedPayload.skip();
        }
        FileHandle file = HermesAssetPaths.internal(path);
        if (!file.exists()) {
            throw new ResourceLoadException("Sound asset not found: " + path);
        }
        return DecodedPayload.fromSourcePath(path);
    }

    @Override
    public Object upload(DecodedPayload decoded) {
        if (decoded.skipped()) {
            return SKIPPED;
        }
        String path = decoded.sourcePath();
        if (path == null || path.isBlank()) {
            throw new ResourceLoadException("Sound decode produced no source path");
        }
        return backend.loadSound(path);
    }

    @Override
    public void dispose(Object resource) {
        // SoundBackend owns clip lifetime; cleared via backend.disposeSounds().
    }
}
