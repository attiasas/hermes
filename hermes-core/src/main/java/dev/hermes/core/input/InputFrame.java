package dev.hermes.core.input;

import java.util.HashSet;
import java.util.Set;

/**
 * Immutable per-frame device snapshot consumed by {@link ActionMapper}. Built by {@code GdxInputReaders}
 * in Task 4; test factories support unit tests without libGDX poll.
 */
public final class InputFrame {

    private final Set<Integer> keyboardPressed;
    private final Set<Integer> keyboardJustPressed;
    private final Set<Integer> keyboardJustReleased;
    private final float pointerX;
    private final float pointerY;
    private final Set<Integer> pointerPressed;
    private final Set<Integer> pointerJustPressed;
    private final Set<Integer> pointerJustReleased;
    private final float[] gamepadAxes;
    private final Set<Integer> gamepadPressed;
    private final Set<Integer> gamepadJustPressed;
    private final Set<Integer> gamepadJustReleased;
    private final int connectedGamepadCount;
    private final float scrollX;
    private final float scrollY;

    private InputFrame(Builder builder) {
        this.keyboardPressed = Set.copyOf(builder.keyboardPressed);
        this.keyboardJustPressed = Set.copyOf(builder.keyboardJustPressed);
        this.keyboardJustReleased = Set.copyOf(builder.keyboardJustReleased);
        this.pointerX = builder.pointerX;
        this.pointerY = builder.pointerY;
        this.pointerPressed = Set.copyOf(builder.pointerPressed);
        this.pointerJustPressed = Set.copyOf(builder.pointerJustPressed);
        this.pointerJustReleased = Set.copyOf(builder.pointerJustReleased);
        this.gamepadAxes = builder.gamepadAxes.clone();
        this.gamepadPressed = Set.copyOf(builder.gamepadPressed);
        this.gamepadJustPressed = Set.copyOf(builder.gamepadJustPressed);
        this.gamepadJustReleased = Set.copyOf(builder.gamepadJustReleased);
        this.connectedGamepadCount = builder.connectedGamepadCount;
        this.scrollX = builder.scrollX;
        this.scrollY = builder.scrollY;
    }

    public int connectedGamepadCount() {
        return connectedGamepadCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static InputFrame pointerJustPressed(float x, float y, int button) {
        return builder()
                .pointer(x, y)
                .pointerPressed(button)
                .pointerJustPressed(button)
                .build();
    }

    public static InputFrame pointerPressed(float x, float y, int button) {
        return builder().pointer(x, y).pointerPressed(button).build();
    }

    /** Pressed button at a new screen position (drag frame). */
    public static InputFrame pointerDrag(float x, float y, int button) {
        return pointerPressed(x, y, button);
    }

    public static InputFrame withKeyboardPressed(int key) {
        return builder().keyboardPressed(key).build();
    }

    public static InputFrame withGamepadAxis(int axis, float value) {
        return builder().gamepadAxis(axis, value).build();
    }

    public static InputFrame pointerScroll(float x, float y, float scrollY) {
        return builder().pointer(x, y).scroll(0f, scrollY).build();
    }

    public float scrollX() {
        return scrollX;
    }

    public float scrollY() {
        return scrollY;
    }

    public boolean keyboardPressed(int key) {
        return keyboardPressed.contains(key);
    }

    public boolean keyboardJustPressed(int key) {
        return keyboardJustPressed.contains(key);
    }

    public boolean keyboardJustReleased(int key) {
        return keyboardJustReleased.contains(key);
    }

    public float pointerX() {
        return pointerX;
    }

    public float pointerY() {
        return pointerY;
    }

    public boolean pointerPressed(int button) {
        return pointerPressed.contains(button);
    }

    public boolean pointerJustPressed(int button) {
        return pointerJustPressed.contains(button);
    }

    public boolean pointerJustReleased(int button) {
        return pointerJustReleased.contains(button);
    }

    public float gamepadAxis(int axis) {
        return gamepadAxis(0, axis);
    }

    public float gamepadAxis(int gamepadIndex, int axis) {
        if (gamepadIndex != 0 || axis < 0 || axis >= gamepadAxes.length) {
            return 0f;
        }
        return gamepadAxes[axis];
    }

    public boolean gamepadPressed(int button) {
        return gamepadPressed.contains(button);
    }

    public boolean gamepadJustPressed(int button) {
        return gamepadJustPressed.contains(button);
    }

    public boolean gamepadJustReleased(int button) {
        return gamepadJustReleased.contains(button);
    }

    public static final class Builder {
        private final Set<Integer> keyboardPressed = new HashSet<>();
        private final Set<Integer> keyboardJustPressed = new HashSet<>();
        private final Set<Integer> keyboardJustReleased = new HashSet<>();
        private float pointerX;
        private float pointerY;
        private final Set<Integer> pointerPressed = new HashSet<>();
        private final Set<Integer> pointerJustPressed = new HashSet<>();
        private final Set<Integer> pointerJustReleased = new HashSet<>();
        private float[] gamepadAxes = new float[GamepadAxisCount.MAX_AXES];
        private final Set<Integer> gamepadPressed = new HashSet<>();
        private final Set<Integer> gamepadJustPressed = new HashSet<>();
        private final Set<Integer> gamepadJustReleased = new HashSet<>();
        private int connectedGamepadCount;
        private float scrollX;
        private float scrollY;

        public Builder connectedGamepadCount(int count) {
            connectedGamepadCount = Math.max(0, count);
            return this;
        }

        public Builder keyboardPressed(int key) {
            keyboardPressed.add(key);
            return this;
        }

        public Builder keyboardJustPressed(int key) {
            keyboardJustPressed.add(key);
            keyboardPressed.add(key);
            return this;
        }

        public Builder keyboardJustReleased(int key) {
            keyboardJustReleased.add(key);
            return this;
        }

        public Builder pointer(float x, float y) {
            pointerX = x;
            pointerY = y;
            return this;
        }

        public Builder pointerPressed(int button) {
            pointerPressed.add(button);
            return this;
        }

        public Builder pointerJustPressed(int button) {
            pointerJustPressed.add(button);
            pointerPressed.add(button);
            return this;
        }

        public Builder pointerJustReleased(int button) {
            pointerJustReleased.add(button);
            return this;
        }

        public Builder scroll(float scrollX, float scrollY) {
            this.scrollX = scrollX;
            this.scrollY = scrollY;
            return this;
        }

        public Builder gamepadAxis(int axis, float value) {
            return gamepadAxis(0, axis, value);
        }

        public Builder gamepadAxis(int gamepadIndex, int axis, float value) {
            if (gamepadIndex == 0 && axis >= 0 && axis < gamepadAxes.length) {
                gamepadAxes[axis] = value;
            }
            return this;
        }

        public Builder gamepadPressed(int button) {
            gamepadPressed.add(button);
            return this;
        }

        public Builder gamepadJustPressed(int button) {
            gamepadJustPressed.add(button);
            gamepadPressed.add(button);
            return this;
        }

        public Builder gamepadJustReleased(int button) {
            gamepadJustReleased.add(button);
            return this;
        }

        public InputFrame build() {
            return new InputFrame(this);
        }
    }

    /** Upper bound for gamepad 0 axis array (matches {@link dev.hermes.api.input.GamepadAxis}). */
    private static final class GamepadAxisCount {
        static final int MAX_AXES = 6;

        private GamepadAxisCount() {
        }
    }
}
