package dev.hermes.gradle;

import dev.hermes.tooling.HermesEngineVersions;
import dev.hermes.tooling.HermesHomeResolver;
import java.io.File;
import java.util.Properties;
import org.gradle.api.initialization.Settings;

/**
 * Copies version properties from a Hermes engine checkout {@code gradle.properties} into game
 * projects that include {@code :hermes-core} / launchers from {@code hermes.home}.
 */
final class HermesEnginePropertyPropagator {

  private HermesEnginePropertyPropagator() {}

  static void applyFromHome(Settings settings, File hermesHome) {
    Properties engineProps = HermesEngineVersions.loadFromEngineHome(hermesHome);
    if (engineProps == null) {
      return;
    }
    settings
        .getGradle()
        .beforeProject(
            project -> {
              for (String key : HermesEngineVersions.GRADLE_PROPERTY_KEYS) {
                String value = engineProps.getProperty(key);
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
