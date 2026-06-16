package dev.hermes.core.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

/** Accumulates mouse wheel deltas from {@link InputProcessor#scrolled(float, float)} for per-frame polling. */
final class ScrollWheelCapture extends InputAdapter {

    private static ScrollWheelCapture shared;

    static ScrollWheelCapture shared() {
        if (shared == null) {
            shared = new ScrollWheelCapture();
        }
        return shared;
    }

    private float scrollX;
    private float scrollY;
    private boolean installed;

    void installIfNeeded() {
        if (installed || Gdx.input == null) {
            return;
        }
        InputProcessor current = Gdx.input.getInputProcessor();
        if (current == this) {
            installed = true;
            return;
        }
        if (current == null) {
            Gdx.input.setInputProcessor(this);
        } else {
            Gdx.input.setInputProcessor(new InputMultiplexer(this, current));
        }
        installed = true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        scrollX += amountX;
        scrollY += amountY;
        return false;
    }

    float takeScrollX() {
        float value = scrollX;
        scrollX = 0f;
        return value;
    }

    float takeScrollY() {
        float value = scrollY;
        scrollY = 0f;
        return value;
    }
}
