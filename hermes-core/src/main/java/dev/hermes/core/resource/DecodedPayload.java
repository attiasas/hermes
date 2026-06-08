package dev.hermes.core.resource;

import com.badlogic.gdx.graphics.Pixmap;

import java.util.Arrays;
import java.util.Objects;

/** CPU-side intermediate produced by Phase A decode; safe to hold off the render thread. */
public final class DecodedPayload {

    private final byte[] bytes;
    private final Pixmap pixmap;

    private DecodedPayload(byte[] bytes, Pixmap pixmap) {
        this.bytes = bytes;
        this.pixmap = pixmap;
    }

    public static DecodedPayload fromBytes(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        return new DecodedPayload(bytes, null);
    }

    public static DecodedPayload fromPixmap(Pixmap pixmap) {
        Objects.requireNonNull(pixmap, "pixmap");
        return new DecodedPayload(null, pixmap);
    }

    public byte[] bytes() {
        return bytes == null ? null : Arrays.copyOf(bytes, bytes.length);
    }

    public Pixmap pixmap() {
        return pixmap;
    }
}
