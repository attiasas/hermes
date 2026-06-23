package dev.hermes.core.resource.loaders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.hermes.api.ecs.SpriteSheet;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.resource.DecodedPayload;
import dev.hermes.core.resource.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Loads sprite sheet grid regions from a texture; cache ref encodes layout metadata. */
public final class SpriteSheetResourceLoader implements ResourceLoader {

    private static final Pattern REF_PATTERN =
            Pattern.compile("^(?<texture>.+)#(?<cols>\\d+)x(?<rows>\\d+):(?<fw>\\d+)x(?<fh>\\d+)$");

    @Override
    public ResourceKind kind() {
        return ResourceKind.SPRITE_SHEET;
    }

    /** Builds a cache ref from texture path and sheet layout. */
    public static ResourceRef ref(String texturePath, SpriteSheet sheet) {
        if (texturePath == null || texturePath.isBlank()) {
            throw new IllegalArgumentException("texturePath must not be blank");
        }
        if (sheet == null) {
            throw new IllegalArgumentException("sheet");
        }
        return ResourceRef.of(
                texturePath
                        + "#"
                        + sheet.columns()
                        + "x"
                        + sheet.rows()
                        + ":"
                        + sheet.frameWidth()
                        + "x"
                        + sheet.frameHeight());
    }

    public static String texturePath(String refPath) {
        return parseRef(refPath).texturePath;
    }

    @Override
    public DecodedPayload decode(String path) {
        SheetRef parsed = parseRef(path);
        FileHandle file = HermesAssetPaths.internal(parsed.texturePath);
        if (!file.exists()) {
            throw new ResourceLoadException("Sprite sheet texture not found: " + parsed.texturePath);
        }
        return DecodedPayload.fromSourcePath(path);
    }

    @Override
    public Object upload(DecodedPayload decoded) {
        String refPath = decoded.sourcePath();
        if (refPath == null || refPath.isBlank()) {
            throw new ResourceLoadException("Sprite sheet decode produced no ref path");
        }
        SheetRef parsed = parseRef(refPath);
        FileHandle file = HermesAssetPaths.internal(parsed.texturePath);
        if (!file.exists()) {
            throw new ResourceLoadException("Sprite sheet texture not found: " + parsed.texturePath);
        }
        byte[] bytes;
        try (InputStream in = file.read()) {
            bytes = in.readAllBytes();
        } catch (IOException e) {
            throw new ResourceLoadException("Failed to read sprite sheet texture: " + parsed.texturePath, e);
        }
        Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
        try {
            Texture texture = new Texture(pixmap);
            return buildRegions(texture, parsed);
        } finally {
            pixmap.dispose();
        }
    }

    @Override
    public void dispose(Object resource) {
        if (resource instanceof TextureRegion[]) {
            TextureRegion[] regions = (TextureRegion[]) resource;
            if (regions.length > 0) {
                regions[0].getTexture().dispose();
            }
        }
    }

    static TextureRegion[] buildRegions(Texture texture, SheetRef sheet) {
        int frameCount = sheet.columns * sheet.rows;
        TextureRegion[] regions = new TextureRegion[frameCount];
        for (int row = 0; row < sheet.rows; row++) {
            for (int col = 0; col < sheet.columns; col++) {
                int index = row * sheet.columns + col;
                int x = col * sheet.frameWidth;
                int y = row * sheet.frameHeight;
                regions[index] = new TextureRegion(texture, x, y, sheet.frameWidth, sheet.frameHeight);
            }
        }
        return regions;
    }

    static SheetRef parseRef(String refPath) {
        if (refPath == null || refPath.isBlank()) {
            throw new ResourceLoadException("Sprite sheet ref must not be blank");
        }
        Matcher matcher = REF_PATTERN.matcher(refPath);
        if (!matcher.matches()) {
            throw new ResourceLoadException("Invalid sprite sheet ref: " + refPath);
        }
        return new SheetRef(
                matcher.group("texture"),
                Integer.parseInt(matcher.group("cols")),
                Integer.parseInt(matcher.group("rows")),
                Integer.parseInt(matcher.group("fw")),
                Integer.parseInt(matcher.group("fh")));
    }

    static final class SheetRef {
        final String texturePath;
        final int columns;
        final int rows;
        final int frameWidth;
        final int frameHeight;

        SheetRef(String texturePath, int columns, int rows, int frameWidth, int frameHeight) {
            this.texturePath = texturePath;
            this.columns = columns;
            this.rows = rows;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
        }
    }
}
