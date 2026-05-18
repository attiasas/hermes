package dev.hermes.tooling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarFile;

/** Resolves Hermes artifacts published to the local Maven repository (~/.m2). */
public final class HermesMavenLocal {

  /** Marker class shipped with the game DSL {@code icons {}} block (Phase 4+). */
  public static final String GRADLE_PLUGIN_ICONS_EXTENSION =
      "dev/hermes/gradle/IconsExtension.class";

  private HermesMavenLocal() {}

  public static File gradlePluginJar(String engineVersion) {
    if (engineVersion == null || engineVersion.isBlank()) {
      engineVersion = "0.1.0-SNAPSHOT";
    }
    return Path.of(
            System.getProperty("user.home"),
            ".m2/repository/dev/hermes/hermes-gradle-plugin",
            engineVersion,
            "hermes-gradle-plugin-" + engineVersion + ".jar")
        .toFile();
  }

  public static boolean gradlePluginJarContains(String engineVersion, String entryName) {
    File jar = gradlePluginJar(engineVersion);
    if (!jar.isFile()) {
      return false;
    }
    try (JarFile jarFile = new JarFile(jar)) {
      return jarFile.getEntry(entryName) != null;
    } catch (IOException e) {
      return false;
    }
  }

  public static boolean gradlePluginSupportsIconsDsl(String engineVersion) {
    return gradlePluginJarContains(engineVersion, GRADLE_PLUGIN_ICONS_EXTENSION);
  }
}
