package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.ecs.ComponentData;

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
}
