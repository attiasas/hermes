package dev.hermes.core.viewport;

import com.badlogic.gdx.Gdx;

public final class GlViewport {

    private GlViewport() {}

    public static void applyFullBackbuffer(int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        Gdx.gl.glViewport(0, 0, w, h);
    }
}
