package dev.hermes.core.input;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.input.GamepadAxis;
import dev.hermes.api.input.GamepadButton;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputKey;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Loads {@link InputProfile} instances from game asset paths. */
public final class InputProfileLoader {

    private InputProfileLoader() {
    }

    public static InputProfile load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new InputProfileParseException("input profile asset path is required");
        }
        FileHandle handle = HermesAssetPaths.internal(assetPath);
        if (!handle.exists()) {
            throw new InputProfileParseException("input profile not found: " + assetPath);
        }
        return parse(handle.readString(StandardCharsets.UTF_8.name()));
    }

    public static InputProfile parse(String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            int version = root.getInt("version", 0);
            if (version < 1) {
                throw new InputProfileParseException("\"version\" must be >= 1");
            }
            String defaultContext = root.getString("context", "").trim();
            if (defaultContext.isEmpty()) {
                throw new InputProfileParseException("\"context\" is required");
            }

            Map<String, InputProfile.ActionType> actions = parseActions(root.get("actions"));
            List<InputProfile.Binding> bindings = parseBindings(root.get("bindings"));
            float deadzone = 0.15f;
            JsonValue gamepad = root.get("gamepad");
            if (gamepad != null && gamepad.isObject()) {
                deadzone = gamepad.getFloat("deadzone", deadzone);
            }
            return new InputProfile(version, defaultContext, actions, bindings, deadzone);
        } catch (InputProfileParseException e) {
            throw e;
        } catch (Exception e) {
            throw new InputProfileParseException("invalid input profile JSON: " + e.getMessage(), e);
        }
    }

    private static Map<String, InputProfile.ActionType> parseActions(JsonValue actionsValue) {
        if (actionsValue == null || !actionsValue.isObject()) {
            throw new InputProfileParseException("\"actions\" object is required");
        }
        Map<String, InputProfile.ActionType> actions = new HashMap<>();
        for (JsonValue entry : actionsValue) {
            String name = entry.name;
            if (name == null || name.isBlank()) {
                throw new InputProfileParseException("action name is required");
            }
            String type = entry.getString("type", "").trim().toLowerCase(Locale.ROOT);
            if ("axis".equals(type)) {
                actions.put(name, InputProfile.ActionType.AXIS);
            } else if ("button".equals(type)) {
                actions.put(name, InputProfile.ActionType.BUTTON);
            } else {
                throw new InputProfileParseException(
                        "action '" + name + "': unknown type '" + type + "'");
            }
        }
        if (actions.isEmpty()) {
            throw new InputProfileParseException("\"actions\" must not be empty");
        }
        return actions;
    }

    private static List<InputProfile.Binding> parseBindings(JsonValue bindingsValue) {
        if (bindingsValue == null || !bindingsValue.isArray()) {
            throw new InputProfileParseException("\"bindings\" array is required");
        }
        List<InputProfile.Binding> bindings = new ArrayList<>();
        for (int i = 0; i < bindingsValue.size; i++) {
            JsonValue entry = bindingsValue.get(i);
            if (!entry.isObject()) {
                throw new InputProfileParseException("bindings[" + i + "] must be an object");
            }
            String action = requireString(entry, "action", "bindings[" + i + "]");
            if (!entry.has("source")) {
                throw new InputProfileParseException("bindings[" + i + "]: \"source\" is required");
            }
            String sourceName = entry.getString("source", "").trim().toLowerCase(Locale.ROOT);
            InputProfile.BindingSource source = parseSource(sourceName, i);
            String context = optionalString(entry, "context");
            InputProfile.BindingWhen when = parseWhen(entry.getString("when", "pressed"), i);
            float scale = entry.getFloat("scale", 1f);

            int key = -1;
            int button = -1;
            int axis = -1;
            int gamepadButton = -1;

            switch (source) {
                case KEYBOARD:
                    key = InputKey.byName(requireString(entry, "key", "bindings[" + i + "]"));
                    break;
                case POINTER:
                    button = InputButton.byName(requireString(entry, "button", "bindings[" + i + "]"));
                    break;
                case GAMEPAD:
                    if (entry.has("axis")) {
                        axis = GamepadAxis.byName(requireString(entry, "axis", "bindings[" + i + "]"));
                    } else if (entry.has("button")) {
                        gamepadButton =
                                GamepadButton.byName(requireString(entry, "button", "bindings[" + i + "]"));
                    } else {
                        throw new InputProfileParseException(
                                "bindings[" + i + "]: gamepad binding requires \"axis\" or \"button\"");
                    }
                    break;
                default:
                    throw new InputProfileParseException("bindings[" + i + "]: unknown source");
            }

            bindings.add(
                    new InputProfile.Binding(
                            action, source, context, when, key, button, axis, gamepadButton, scale));
        }
        return bindings;
    }

    private static InputProfile.BindingSource parseSource(String sourceName, int index) {
        switch (sourceName) {
            case "keyboard":
                return InputProfile.BindingSource.KEYBOARD;
            case "pointer":
                return InputProfile.BindingSource.POINTER;
            case "gamepad":
                return InputProfile.BindingSource.GAMEPAD;
            default:
                throw new InputProfileParseException(
                        "bindings[" + index + "]: unknown source '" + sourceName + "'");
        }
    }

    private static InputProfile.BindingWhen parseWhen(String whenName, int index) {
        String normalized = whenName == null ? "" : whenName.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "":
            case "pressed":
                return InputProfile.BindingWhen.PRESSED;
            case "justpressed":
                return InputProfile.BindingWhen.JUST_PRESSED;
            case "justreleased":
                return InputProfile.BindingWhen.JUST_RELEASED;
            default:
                throw new InputProfileParseException(
                        "bindings[" + index + "]: unknown when '" + whenName + "'");
        }
    }

    private static String requireString(JsonValue object, String field, String context) {
        if (!object.has(field)) {
            throw new InputProfileParseException(context + ": \"" + field + "\" is required");
        }
        String value = object.getString(field, "").trim();
        if (value.isEmpty()) {
            throw new InputProfileParseException(context + ": \"" + field + "\" must be non-empty");
        }
        return value;
    }

    private static String optionalString(JsonValue object, String field) {
        if (!object.has(field)) {
            return null;
        }
        String value = object.getString(field, "").trim();
        return value.isEmpty() ? null : value;
    }
}
