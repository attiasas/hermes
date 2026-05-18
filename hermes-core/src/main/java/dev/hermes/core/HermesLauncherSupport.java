package dev.hermes.core;

import dev.hermes.api.HermesApplication;

/** Loads the user {@link HermesApplication} from launch configuration (JVM properties or packaged properties). */
public final class HermesLauncherSupport {

  private HermesLauncherSupport() {}

  public static HermesApplication loadApplication() {
    String className = HermesRuntimeConfig.get("hermes.applicationClass", "");
    if (className.isBlank()) {
      throw new IllegalStateException(
          "hermes.applicationClass is required (set by the Hermes Gradle plugin or hermes-runtime.properties).");
    }
    try {
      Class<?> type = Class.forName(className);
      Object instance = type.getDeclaredConstructor().newInstance();
      if (!(instance instanceof HermesApplication)) {
        throw new IllegalStateException(className + " does not implement HermesApplication");
      }
      return (HermesApplication) instance;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to load Hermes application: " + className, e);
    }
  }

  public static boolean isDebugEnabled() {
    return Boolean.parseBoolean(HermesRuntimeConfig.get("hermes.debug", "false"));
  }

  public static int windowWidth() {
    return Integer.parseInt(HermesRuntimeConfig.get("hermes.window.width", "640"));
  }

  public static int windowHeight() {
    return Integer.parseInt(HermesRuntimeConfig.get("hermes.window.height", "480"));
  }

  public static String windowTitle() {
    return HermesRuntimeConfig.get("hermes.window.title", "Hermes");
  }
}
