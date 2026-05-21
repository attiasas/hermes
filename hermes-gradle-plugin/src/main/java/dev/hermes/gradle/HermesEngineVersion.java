package dev.hermes.gradle;

import dev.hermes.gradle.dsl.HermesConfig;
import dev.hermes.gradle.dsl.HermesSettingsExtension;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

/** Resolves the Hermes engine artifact version for Maven coordinates. */
public final class HermesEngineVersion {

  public static final String GRADLE_PROPERTY = "hermes.engineVersion";
  public static final String DEFAULT_GROUP = "dev.hermes";

  private HermesEngineVersion() {}

  public static String resolve(Project project) {
    return HermesConfig.resolveEngineVersion(project);
  }

  public static String resolve(Settings settings, HermesSettingsExtension extension) {
    if (extension.getEngineVersion() != null && !extension.getEngineVersion().isBlank()) {
      return extension.getEngineVersion();
    }
    String fromProperty = readGradleProperty(settings, GRADLE_PROPERTY);
    if (fromProperty != null && !fromProperty.isBlank()) {
      return fromProperty;
    }
    return "0.1.0-SNAPSHOT";
  }

  private static String readProperty(Project project, String name) {
    if (project.hasProperty(name)) {
      return project.property(name).toString();
    }
    Project root = project.getRootProject();
    if (root != project && root.hasProperty(name)) {
      return root.property(name).toString();
    }
    return null;
  }

  private static String readGradleProperty(Settings settings, String name) {
    try {
      return settings.getProviders().gradleProperty(name).getOrNull();
    } catch (Exception ignored) {
      return null;
    }
  }
}
