package dev.hermes.gradle.dsl;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeExtension {

    private final Map<String, String> values = new LinkedHashMap<>();

    public void put(String key, String value) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        values.put(key, value);
    }

    public Map<String, String> asMap() {
        return Map.copyOf(values);
    }
}
