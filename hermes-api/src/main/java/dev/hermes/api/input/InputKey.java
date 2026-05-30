package dev.hermes.api.input;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Keyboard key codes for input bindings. Values match libGDX {@link com.badlogic.gdx.Input.Keys}.
 */
public final class InputKey {

    public static final int A = 29;
    public static final int D = 32;
    public static final int S = 47;
    public static final int W = 51;
    public static final int SPACE = 62;
    public static final int ENTER = 66;
    public static final int Q = 45;
    public static final int F1 = 131;
    public static final int ESCAPE = 111;

    public static final int UP = 19;
    public static final int DOWN = 20;
    public static final int LEFT = 21;
    public static final int RIGHT = 22;

    private static final Map<String, Integer> BY_NAME = new HashMap<>();

    static {
        register("A", A);
        register("D", D);
        register("S", S);
        register("W", W);
        register("SPACE", SPACE);
        register("ENTER", ENTER);
        register("Q", Q);
        register("F1", F1);
        register("ESCAPE", ESCAPE);
        register("UP", UP);
        register("DOWN", DOWN);
        register("LEFT", LEFT);
        register("RIGHT", RIGHT);
    }

    private InputKey() {
    }

    public static int byName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("key name is required");
        }
        Integer code = BY_NAME.get(name.trim().toUpperCase(Locale.ROOT));
        if (code == null) {
            throw new IllegalArgumentException("unknown key: " + name);
        }
        return code;
    }

    private static void register(String name, int code) {
        BY_NAME.put(name, code);
    }
}
