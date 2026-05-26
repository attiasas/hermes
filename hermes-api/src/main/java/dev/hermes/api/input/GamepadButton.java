package dev.hermes.api.input;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Gamepad button codes for input bindings. Values match libGDX {@link com.badlogic.gdx.Input.Keys}
 * gamepad button constants (same codes as {@code Controller} buttons on Android/desktop).
 */
public final class GamepadButton {

    public static final int A = 96;
    public static final int B = 97;
    public static final int X = 99;
    public static final int Y = 100;
    public static final int L1 = 102;
    public static final int R1 = 103;
    public static final int START = 108;
    public static final int SELECT = 109;

    private static final Map<String, Integer> BY_NAME = new HashMap<>();

    static {
        register("A", A);
        register("B", B);
        register("X", X);
        register("Y", Y);
        register("L1", L1);
        register("R1", R1);
        register("START", START);
        register("SELECT", SELECT);
    }

    private GamepadButton() {
    }

    public static int byName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("gamepad button name is required");
        }
        Integer code = BY_NAME.get(name.trim().toUpperCase(Locale.ROOT));
        if (code == null) {
            throw new IllegalArgumentException("unknown gamepad button: " + name);
        }
        return code;
    }

    private static void register(String name, int code) {
        BY_NAME.put(name, code);
    }
}
