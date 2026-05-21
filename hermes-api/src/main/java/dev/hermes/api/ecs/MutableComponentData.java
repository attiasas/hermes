package dev.hermes.api.ecs;

import java.util.HashMap;
import java.util.Map;

/** Mutable component property bag used by serializers and the inspector. */
public final class MutableComponentData implements ComponentData {

  private final Map<String, Object> values = new HashMap<>();

  public void putFloat(String key, float value) {
    values.put(key, value);
  }

  public void putDouble(String key, double value) {
    values.put(key, value);
  }

  public void putInt(String key, int value) {
    values.put(key, value);
  }

  public void putBoolean(String key, boolean value) {
    values.put(key, value);
  }

  public void putString(String key, String value) {
    values.put(key, value);
  }

  public void put(String key, Object value) {
    values.put(key, value);
  }

  @Override
  public boolean has(String key) {
    return values.containsKey(key);
  }

  @Override
  public double getDouble(String key, double defaultValue) {
    Object value = values.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    return Double.parseDouble(value.toString());
  }

  @Override
  public float getFloat(String key, float defaultValue) {
    return (float) getDouble(key, defaultValue);
  }

  @Override
  public int getInt(String key, int defaultValue) {
    Object value = values.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    return Integer.parseInt(value.toString());
  }

  @Override
  public String getString(String key, String defaultValue) {
    Object value = values.get(key);
    return value == null ? defaultValue : value.toString();
  }

  @Override
  public boolean getBoolean(String key, boolean defaultValue) {
    Object value = values.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value instanceof Number) {
      return ((Number) value).intValue() != 0;
    }
    return Boolean.parseBoolean(value.toString());
  }
}
