package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.ecs.ComponentData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class JsonComponentData implements ComponentData {

    private final JsonValue object;

    JsonComponentData(JsonValue object) {
        this.object = object == null ? new JsonValue(JsonValue.ValueType.object) : object;
    }

    @Override
    public boolean has(String key) {
        return object.has(key);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? defaultValue : value.asDouble();
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return (float) getDouble(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? defaultValue : value.asInt();
    }

    @Override
    public String getString(String key, String defaultValue) {
        JsonValue value = object.get(key);
        return value == null || value.isNull() ? defaultValue : value.asString();
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        JsonValue value = object.get(key);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isNumber()) {
            return value.asInt() != 0;
        }
        return Boolean.parseBoolean(value.asString());
    }

    Map<String, float[]> getFloatArrayMap(String key) {
        JsonValue map = object.get(key);
        if (map == null || !map.isObject()) {
            return Collections.emptyMap();
        }
        Map<String, float[]> result = new HashMap<>();
        for (JsonValue entry : map) {
            result.put(entry.name, toFloatArray(entry));
        }
        return result;
    }

    private static float[] toFloatArray(JsonValue value) {
        if (value == null || value.isNull()) {
            return new float[0];
        }
        if (value.isArray()) {
            float[] arr = new float[value.size];
            for (int i = 0; i < value.size; i++) {
                arr[i] = value.getFloat(i);
            }
            return arr;
        }
        if (value.isNumber()) {
            return new float[]{value.asFloat()};
        }
        return new float[0];
    }
}
