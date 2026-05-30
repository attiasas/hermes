package dev.hermes.api.input;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Pointer (mouse/touch) button codes for input bindings. Values match libGDX {@link com.badlogic.gdx.Input.Buttons}.
 */
public final class InputButton {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int MIDDLE = 2;

    private static final Map<String, Integer> BY_NAME = new HashMap<>();

    static {
        register("LEFT", LEFT);
        register("RIGHT", RIGHT);
        register("MIDDLE", MIDDLE);
    }

    private InputButton() {
    }

    public static int byName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("button name is required");
        }
        Integer code = BY_NAME.get(name.trim().toUpperCase(Locale.ROOT));
        if (code == null) {
            throw new IllegalArgumentException("unknown button: " + name);
        }
        return code;
    }

    private static void register(String name, int code) {
        BY_NAME.put(name, code);
    }
}
