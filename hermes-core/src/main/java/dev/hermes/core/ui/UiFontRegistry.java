package dev.hermes.core.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

/** Resolves {@link BitmapFont} instances for UI labels and buttons. */
public final class UiFontRegistry implements Disposable {

    private BitmapFont defaultFont;

    public BitmapFont defaultFont() {
        if (defaultFont == null) {
            defaultFont = new BitmapFont();
        }
        return defaultFont;
    }

    public BitmapFont resolve(String fontId) {
        if (fontId == null || fontId.isBlank()) {
            return defaultFont();
        }
        return defaultFont();
    }

    @Override
    public void dispose() {
        if (defaultFont != null) {
            defaultFont.dispose();
            defaultFont = null;
        }
    }
}
