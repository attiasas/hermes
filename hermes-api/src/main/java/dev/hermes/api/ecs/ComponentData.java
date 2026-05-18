package dev.hermes.api.ecs;

/** Read-only view of a component's JSON properties during deserialization. */
public interface ComponentData {

  boolean has(String key);

  double getDouble(String key, double defaultValue);

  float getFloat(String key, float defaultValue);

  int getInt(String key, int defaultValue);

  String getString(String key, String defaultValue);
}
