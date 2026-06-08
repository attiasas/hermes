package dev.hermes.core.resource;

import com.badlogic.gdx.graphics.Pixmap;

import java.util.Arrays;
import java.util.Objects;

/** CPU-side intermediate produced by Phase A decode; safe to hold off the render thread. */
public final class DecodedPayload {

    private final byte[] bytes;
    private final Pixmap pixmap;
    private final String sourcePath;
    private final boolean skipped;

    private DecodedPayload(byte[] bytes, Pixmap pixmap, String sourcePath, boolean skipped) {
        this.bytes = bytes;
        this.pixmap = pixmap;
        this.sourcePath = sourcePath;
        this.skipped = skipped;
    }

    public static DecodedPayload fromBytes(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        return new DecodedPayload(bytes, null, null, false);
    }

    public static DecodedPayload fromPixmap(Pixmap pixmap) {
        Objects.requireNonNull(pixmap, "pixmap");
        return new DecodedPayload(null, pixmap, null, false);
    }

    /** Carries an asset path for upload phases that must resolve companion files (OBJ, sound). */
    public static DecodedPayload fromSourcePath(String sourcePath) {
        Objects.requireNonNull(sourcePath, "sourcePath");
        if (sourcePath.isBlank()) {
            throw new IllegalArgumentException("sourcePath must not be blank");
        }
        return new DecodedPayload(null, null, sourcePath, false);
    }

    /** Marks a resource intentionally skipped (e.g. sound on HTML). */
    public static DecodedPayload skip() {
        return new DecodedPayload(null, null, null, true);
    }

    public byte[] bytes() {
        return bytes == null ? null : Arrays.copyOf(bytes, bytes.length);
    }

    public Pixmap pixmap() {
        return pixmap;
    }

    public String sourcePath() {
        return sourcePath;
    }

    public boolean skipped() {
        return skipped;
    }
}
