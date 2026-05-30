package dev.hermes.core.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import dev.hermes.core.HermesAssetPaths;
import java.util.HashMap;
import java.util.Map;

/** Loads and caches textures for UI images and solid fills. */
public final class UiTextureCache implements Disposable {

    private final Map<String, TextureRegion> regionsByPath = new HashMap<>();
    private TextureRegion whitePixel;

    public TextureRegion whitePixel() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            whitePixel = new TextureRegion(texture);
        }
        return whitePixel;
    }

    public TextureRegion texture(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            return whitePixel();
        }
        return regionsByPath.computeIfAbsent(
                assetPath,
                path -> {
                    FileHandle file = HermesAssetPaths.internal(path);
                    return new TextureRegion(new Texture(file));
                });
    }

    @Override
    public void dispose() {
        if (whitePixel != null) {
            whitePixel.getTexture().dispose();
            whitePixel = null;
        }
        for (TextureRegion region : regionsByPath.values()) {
            region.getTexture().dispose();
        }
        regionsByPath.clear();
    }
}
