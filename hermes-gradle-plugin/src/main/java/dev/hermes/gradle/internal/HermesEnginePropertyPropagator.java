package dev.hermes.gradle.internal;

import dev.hermes.tooling.HermesEngineVersions;
import java.io.File;
import java.util.Properties;
import org.gradle.api.initialization.Settings;

/** Injects libGDX / launcher version properties into all Gradle projects when unset. */
public final class HermesEnginePropertyPropagator {

  private HermesEnginePropertyPropagator() {}

  public static void apply(Settings settings, File hermesHome) {
    Properties resolved = HermesEngineVersions.defaults();
    Properties fromHome = HermesEngineVersions.loadFromEngineHome(hermesHome);
    if (fromHome != null) {
      for (String key : HermesEngineVersions.GRADLE_PROPERTY_KEYS) {
        String value = fromHome.getProperty(key);
        if (value != null && !value.isBlank()) {
          resolved.setProperty(key, value);
        }
      }
    }
    settings
        .getGradle()
        .beforeProject(
            project -> {
              for (String key : HermesEngineVersions.GRADLE_PROPERTY_KEYS) {
                String value = resolved.getProperty(key);
                if (value == null || value.isBlank()) {
                  continue;
                }
                if (project.findProperty(key) != null) {
                  continue;
                }
                project.getExtensions().getExtraProperties().set(key, value);
              }
            });
  }
}
