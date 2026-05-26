package dev.hermes.api.input;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Gamepad axis codes for input bindings. Values match libGDX {@code Controller} axis constants
 * ({@code getAxis} / {@code Controllers} extension).
 */
public final class GamepadAxis {

    public static final int LEFT_X = 0;
    public static final int LEFT_Y = 1;
    public static final int RIGHT_X = 2;
    public static final int RIGHT_Y = 3;
    public static final int LEFT_TRIGGER = 4;
    public static final int RIGHT_TRIGGER = 5;

    private static final Map<String, Integer> BY_NAME = new HashMap<>();

    static {
        register("LEFT_X", LEFT_X);
        register("LEFT_Y", LEFT_Y);
        register("RIGHT_X", RIGHT_X);
        register("RIGHT_Y", RIGHT_Y);
        register("LEFT_TRIGGER", LEFT_TRIGGER);
        register("RIGHT_TRIGGER", RIGHT_TRIGGER);
    }

    private GamepadAxis() {
    }

    public static int byName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("axis name is required");
        }
        Integer code = BY_NAME.get(name.trim().toUpperCase(Locale.ROOT));
        if (code == null) {
            throw new IllegalArgumentException("unknown axis: " + name);
        }
        return code;
    }

    private static void register(String name, int code) {
        BY_NAME.put(name, code);
    }
}
