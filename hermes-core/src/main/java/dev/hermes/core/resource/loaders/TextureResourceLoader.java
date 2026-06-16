package dev.hermes.core.resource.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.resource.DecodedPayload;
import dev.hermes.core.resource.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

/** Loads PNG/JPG textures via Pixmap decode and Texture upload. */
public final class TextureResourceLoader implements ResourceLoader {

    @Override
    public ResourceKind kind() {
        return ResourceKind.TEXTURE;
    }

    @Override
    public DecodedPayload decode(String path) {
        FileHandle file = HermesAssetPaths.internal(path);
        if (!file.exists()) {
            throw new ResourceLoadException("Texture asset not found: " + path);
        }
        try (InputStream in = file.read()) {
            return DecodedPayload.fromBytes(in.readAllBytes());
        } catch (IOException e) {
            throw new ResourceLoadException("Failed to read texture: " + path, e);
        } catch (RuntimeException e) {
            throw new ResourceLoadException("Failed to decode texture: " + path, e);
        }
    }

    @Override
    public Object upload(DecodedPayload decoded) {
        Pixmap pixmap = decoded.pixmap();
        if (pixmap == null) {
            byte[] bytes = decoded.bytes();
            if (bytes == null || bytes.length == 0) {
                throw new ResourceLoadException("Texture decode produced no pixmap or bytes");
            }
            pixmap = new Pixmap(bytes, 0, bytes.length);
        }
        try {
            Texture texture = new Texture(pixmap);
            return new TextureRegion(texture);
        } finally {
            pixmap.dispose();
        }
    }

    @Override
    public void dispose(Object resource) {
        if (resource instanceof TextureRegion) {
            ((TextureRegion) resource).getTexture().dispose();
        }
    }
}
