package dev.hermes.core.input;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Parsed input profile from {@code assets/input/profile.json}. */
public final class InputProfile {

    public enum ActionType {
        AXIS,
        BUTTON
    }

    public enum BindingSource {
        KEYBOARD,
        POINTER,
        GAMEPAD
    }

    public enum BindingWhen {
        PRESSED,
        JUST_PRESSED,
        JUST_RELEASED
    }

    private final int version;
    private final String defaultContext;
    private final Map<String, ActionType> actions;
    private final List<Binding> bindings;
    private final float gamepadDeadzone;

    InputProfile(
            int version,
            String defaultContext,
            Map<String, ActionType> actions,
            List<Binding> bindings,
            float gamepadDeadzone) {
        this.version = version;
        this.defaultContext = defaultContext;
        this.actions = Collections.unmodifiableMap(actions);
        this.bindings = Collections.unmodifiableList(bindings);
        this.gamepadDeadzone = gamepadDeadzone;
    }

    public int version() {
        return version;
    }

    public String defaultContext() {
        return defaultContext;
    }

    public Map<String, ActionType> actions() {
        return actions;
    }

    public List<Binding> bindings() {
        return bindings;
    }

    public float gamepadDeadzone() {
        return gamepadDeadzone;
    }

    public static final class Binding {
        private final String action;
        private final BindingSource source;
        private final String context;
        private final BindingWhen when;
        private final int key;
        private final int button;
        private final int axis;
        private final int gamepadButton;
        private final float scale;

        Binding(
                String action,
                BindingSource source,
                String context,
                BindingWhen when,
                int key,
                int button,
                int axis,
                int gamepadButton,
                float scale) {
            this.action = action;
            this.source = source;
            this.context = context;
            this.when = when;
            this.key = key;
            this.button = button;
            this.axis = axis;
            this.gamepadButton = gamepadButton;
            this.scale = scale;
        }

        public String action() {
            return action;
        }

        public BindingSource source() {
            return source;
        }

        public String context() {
            return context;
        }

        public BindingWhen when() {
            return when;
        }

        public int key() {
            return key;
        }

        public int button() {
            return button;
        }

        public int axis() {
            return axis;
        }

        public int gamepadButton() {
            return gamepadButton;
        }

        public float scale() {
            return scale;
        }
    }
}
