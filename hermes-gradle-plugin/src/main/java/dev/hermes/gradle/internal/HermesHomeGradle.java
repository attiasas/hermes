package dev.hermes.gradle.internal;

import dev.hermes.gradle.dsl.HermesSettingsExtension;
import dev.hermes.tooling.gradle.HermesHomeResolver;
import java.io.File;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

/** Gradle adapter: maps Settings/Project to {@link HermesHomeResolver}. */
public final class HermesHomeGradle {

  private HermesHomeGradle() {}

  public static File resolve(Settings settings) {
    HermesSettingsExtension extension = settings.getExtensions().findByType(HermesSettingsExtension.class);
    File configured = extension != null ? extension.getHome() : null;
    String fromGradle = readGradleProperty(settings, HermesHomeResolver.GRADLE_PROPERTY);
    return HermesHomeResolver.resolve(settings.getRootDir().toPath(), configured, fromGradle);
  }

  public static File resolve(Project project) {
    String fromGradle = readProjectProperty(project, HermesHomeResolver.GRADLE_PROPERTY);
    return HermesHomeResolver.resolve(project.getRootDir().toPath(), null, fromGradle);
  }

  public static boolean isHermesCheckout(File home) {
    return HermesHomeResolver.isHermesCheckout(home);
  }

  private static String readGradleProperty(Settings settings, String name) {
    try {
      return settings.getProviders().gradleProperty(name).getOrNull();
    } catch (Exception ignored) {
      return null;
    }
  }

  private static String readProjectProperty(Project project, String name) {
    if (project.hasProperty(name)) {
      Object value = project.property(name);
      return value == null ? null : value.toString();
    }
    Project root = project.getRootProject();
    if (root != project && root.hasProperty(name)) {
      Object value = root.property(name);
      return value == null ? null : value.toString();
    }
    return null;
  }
}
