package dev.hermes.gradle;

import java.io.File;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

/** Resolves Hermes engine checkout from {@code hermes.home}, {@code HERMES_HOME}, or DSL. */
public final class HermesHomeResolver {

  public static final String ENV_HERMES_HOME = "HERMES_HOME";
  public static final String GRADLE_PROPERTY = "hermes.home";

  private HermesHomeResolver() {}

  public static File resolve(Settings settings) {
    HermesSettingsExtension extension = settings.getExtensions().findByType(HermesSettingsExtension.class);
    if (extension != null && extension.getHome() != null) {
      File configured = extension.getHome();
      if (configured.isDirectory()) {
        return configured.getAbsoluteFile();
      }
    }
    return resolveFromEnvironment(settings);
  }

  public static File resolve(Project project) {
    HermesExtension extension = project.getExtensions().findByType(HermesExtension.class);
    if (extension != null && extension.getHome() != null) {
      File configured = extension.getHome();
      if (configured.isDirectory()) {
        return configured.getAbsoluteFile();
      }
    }
    return resolveFromEnvironment(project);
  }

  private static File resolveFromEnvironment(Settings settings) {
    String fromGradle = readGradleProperty(settings, GRADLE_PROPERTY);
    String fromEnv = System.getenv(ENV_HERMES_HOME);
    String path = firstNonBlank(fromGradle, fromEnv);
    if (path == null || path.isBlank()) {
      return null;
    }
    File home = new File(path);
    return home.isDirectory() ? home.getAbsoluteFile() : null;
  }

  private static File resolveFromEnvironment(Project project) {
    String fromGradle = readProjectProperty(project, GRADLE_PROPERTY);
    String fromEnv = System.getenv(ENV_HERMES_HOME);
    String path = firstNonBlank(fromGradle, fromEnv);
    if (path == null || path.isBlank()) {
      return null;
    }
    File home = new File(path);
    return home.isDirectory() ? home.getAbsoluteFile() : null;
  }

  public static boolean isHermesCheckout(File home) {
    return home != null
        && home.isDirectory()
        && new File(home, "hermes-api").isDirectory()
        && new File(home, "hermes-core").isDirectory();
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

  private static String firstNonBlank(String first, String second) {
    if (first != null && !first.isBlank()) {
      return first;
    }
    if (second != null && !second.isBlank()) {
      return second;
    }
    return null;
  }
}
