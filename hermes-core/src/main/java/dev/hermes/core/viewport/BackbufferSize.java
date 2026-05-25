package dev.hermes.core.viewport;

import com.badlogic.gdx.Gdx;
import dev.hermes.core.HermesLauncherSupport;

public final class BackbufferSize {

    private BackbufferSize() {}

    public static int width() {
        int w = Gdx.graphics.getBackBufferWidth();
        if (w <= 0) {
            w = Gdx.graphics.getWidth();
        }
        if (w <= 0) {
            w = HermesLauncherSupport.windowWidth();
        }
        return Math.max(1, w);
    }

    public static int height() {
        int h = Gdx.graphics.getBackBufferHeight();
        if (h <= 0) {
            h = Gdx.graphics.getHeight();
        }
        if (h <= 0) {
            h = HermesLauncherSupport.windowHeight();
        }
        return Math.max(1, h);
    }
}
