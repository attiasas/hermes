package dev.hermes.gradle;

import org.gradle.api.initialization.Settings;

/** Registers the Android Gradle Plugin for synced {@code hermes-launcher-android} projects. */
final class HermesAndroidGradlePlugin {

  static final String PLUGIN_ID = "com.android.application";
  static final String VERSION_PROPERTY = "hermes.androidGradlePluginVersion";
  static final String DEFAULT_VERSION = "8.9.3";

  private HermesAndroidGradlePlugin() {}

  static void register(Settings settings) {
    String version = readGradleProperty(settings, VERSION_PROPERTY);
    if (version == null || version.isBlank()) {
      version = DEFAULT_VERSION;
    }
    settings.getPluginManagement().getPlugins().id(PLUGIN_ID).version(version);
  }

  private static String readGradleProperty(Settings settings, String name) {
    try {
      return settings.getProviders().gradleProperty(name).getOrNull();
    } catch (Exception ignored) {
      return null;
    }
  }
}
