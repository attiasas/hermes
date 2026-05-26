package dev.hermes.core.input;

import dev.hermes.api.input.InputActions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Mutable remapped actions for the current frame; updated by {@link ActionMapper#apply}. */
public final class InputActionsState implements InputActions {

    private String context = "";
    private final Map<String, Float> axes = new HashMap<>();
    private final Map<String, Boolean> pressed = new HashMap<>();
    private final Map<String, Boolean> justPressed = new HashMap<>();
    private final Map<String, Boolean> justReleased = new HashMap<>();
    private final Map<String, Boolean> previousPressed = new HashMap<>();

    void beginFrame(String activeContext) {
        context = activeContext == null ? "" : activeContext;
        previousPressed.clear();
        previousPressed.putAll(pressed);
        axes.clear();
        pressed.clear();
        justPressed.clear();
        justReleased.clear();
    }

    void setAxis(String action, float value) {
        axes.put(action, value);
    }

    void addAxis(String action, float delta) {
        axes.merge(action, delta, Float::sum);
    }

    void setPressed(String action, boolean value) {
        if (value) {
            pressed.put(action, true);
        }
    }

    void setJustPressed(String action) {
        justPressed.put(action, true);
        pressed.put(action, true);
    }

    void setJustReleased(String action) {
        justReleased.put(action, true);
    }

    void finalizeEdges(Set<String> buttonActions) {
        for (String action : buttonActions) {
            boolean now = pressed.getOrDefault(action, false);
            boolean before = previousPressed.getOrDefault(action, false);
            if (now && !before && !justPressed.containsKey(action)) {
                justPressed.put(action, true);
            }
            if (!now && before && !justReleased.containsKey(action)) {
                justReleased.put(action, true);
            }
        }
    }

    @Override
    public boolean pressed(String action) {
        return pressed.getOrDefault(action, false);
    }

    @Override
    public boolean justPressed(String action) {
        return justPressed.getOrDefault(action, false);
    }

    @Override
    public boolean justReleased(String action) {
        return justReleased.getOrDefault(action, false);
    }

    @Override
    public float axis(String action) {
        return axes.getOrDefault(action, 0f);
    }

    @Override
    public void axis2(String action, float[] out) {
        if (out == null || out.length < 2) {
            return;
        }
        out[0] = axis(action + "_x");
        out[1] = axis(action + "_y");
    }

    @Override
    public String context() {
        return context;
    }
}
