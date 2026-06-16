package dev.hermes.core.resource;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/** Test SPI loader for {@link ResourceKind#BINARY} assets. */
public final class TestBinaryResourceLoader implements ResourceLoader {

    @Override
    public ResourceKind kind() {
        return ResourceKind.BINARY;
    }

    @Override
    public DecodedPayload decode(String path) {
        if (!path.endsWith(".bin")) {
            throw new ResourceLoadException("Test binary loader only handles .bin paths: " + path);
        }
        FileHandle file = HermesAssetPaths.internal(path);
        if (!file.exists()) {
            throw new ResourceLoadException("Binary asset not found: " + path);
        }
        try (InputStream in = file.read()) {
            return DecodedPayload.fromBytes(in.readAllBytes());
        } catch (IOException e) {
            throw new ResourceLoadException("Failed to read binary asset: " + path, e);
        }
    }

    @Override
    public Object upload(DecodedPayload decoded) {
        byte[] bytes = decoded.bytes();
        if (bytes == null || bytes.length == 0) {
            throw new ResourceLoadException("Binary decode produced no bytes");
        }
        return Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public void dispose(Object resource) {
        // byte[] payload needs no disposal
    }
}
