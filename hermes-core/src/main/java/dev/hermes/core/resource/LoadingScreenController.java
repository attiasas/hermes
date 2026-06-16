package dev.hermes.core.resource;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.Objects;
import java.util.function.Supplier;

/** Fullscreen loading overlay shown while async resource batches are in flight. */
public final class LoadingScreenController implements Disposable {

    private final BuiltinLoadingScreen builtin;
    private final String customUiPath;

    private boolean visible;
    private Supplier<Float> progressSupplier;
    private String label;

    public LoadingScreenController() {
        this(null);
    }

    /** @param customUiPath optional UI document path; custom rendering is not wired in v1 */
    public LoadingScreenController(String customUiPath) {
        this.builtin = new BuiltinLoadingScreen();
        this.customUiPath = customUiPath;
    }

    public void begin(Supplier<Float> progressSupplier, String label) {
        this.progressSupplier = Objects.requireNonNull(progressSupplier, "progressSupplier");
        this.label = label != null ? label : "";
        visible = true;
    }

    public void end() {
        visible = false;
        progressSupplier = null;
        label = null;
    }

    public boolean isVisible() {
        return visible;
    }

    public void render(SpriteBatch batch, int width, int height) {
        if (!visible || batch == null || width <= 0 || height <= 0) {
            return;
        }
        float progress = progressSupplier != null ? progressSupplier.get() : 0f;
        // v1: always use built-in bar; customUiPath reserved for future UiDocument integration.
        if (customUiPath != null && !customUiPath.isBlank()) {
            // stub — fall through to builtin until custom UI bindings are wired
        }
        builtin.render(batch, width, height, progress, label != null ? label : "");
    }

    @Override
    public void dispose() {
        builtin.dispose();
    }
}
