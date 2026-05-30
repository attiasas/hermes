package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonValue;

import java.util.Map;

/** Deep-merges component JSON: template base, instance overrides win on conflict. */
final class ComponentMerge {

    private ComponentMerge() {
    }

    static JsonValue merge(JsonValue template, JsonValue instance) {
        JsonValue base = template != null && template.isObject() ? template : emptyObject();
        JsonValue overrides = instance != null && instance.isObject() ? instance : emptyObject();
        JsonValue result = emptyObject();
        for (JsonValue entry : base) {
            result.addChild(entry.name, copyValue(entry));
        }
        for (JsonValue entry : overrides) {
            JsonValue existing = result.get(entry.name);
            if (existing != null && existing.isObject() && entry.isObject()) {
                result.remove(entry.name);
                result.addChild(entry.name, merge(existing, entry));
            } else {
                if (existing != null) {
                    result.remove(entry.name);
                }
                result.addChild(entry.name, copyValue(entry));
            }
        }
        return result;
    }

    static JsonValue toJsonObject(Map<String, JsonValue> components) {
        JsonValue object = emptyObject();
        if (components == null) {
            return object;
        }
        for (Map.Entry<String, JsonValue> entry : components.entrySet()) {
            JsonValue value = entry.getValue();
            object.addChild(entry.getKey(), value == null ? emptyObject() : copyValue(value));
        }
        return object;
    }

    private static JsonValue emptyObject() {
        return new JsonValue(JsonValue.ValueType.object);
    }

    private static JsonValue copyValue(JsonValue value) {
        if (value == null || value.isNull()) {
            return new JsonValue(value);
        }
        if (value.isObject()) {
            JsonValue copy = emptyObject();
            for (JsonValue child : value) {
                copy.addChild(child.name, copyValue(child));
            }
            return copy;
        }
        if (value.isArray()) {
            JsonValue copy = new JsonValue(JsonValue.ValueType.array);
            for (int i = 0; i < value.size; i++) {
                copy.addChild(copyValue(value.get(i)));
            }
            return copy;
        }
        if (value.isBoolean()) {
            return new JsonValue(value.asBoolean());
        }
        if (value.isNumber()) {
            return new JsonValue(value.asDouble());
        }
        if (value.isString()) {
            return new JsonValue(value.asString());
        }
        return new JsonValue(value.toString());
    }
}
