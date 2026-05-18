package dev.hermes.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.tooling.HermesMavenLocal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesMavenLocalTest {

  @Test
  void gradlePluginSupportsIconsDsl_detectsMarkerClass(@TempDir Path temp) throws Exception {
    Path repo = temp.resolve(".m2/repository/dev/hermes/hermes-gradle-plugin/9.9.9");
    Files.createDirectories(repo);
    Path jar = repo.resolve("hermes-gradle-plugin-9.9.9.jar");
    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
      out.putNextEntry(new JarEntry(HermesMavenLocal.GRADLE_PLUGIN_ICONS_EXTENSION));
      out.closeEntry();
    }

    String previousHome = System.getProperty("user.home");
    System.setProperty("user.home", temp.toString());
    try {
      assertTrue(HermesMavenLocal.gradlePluginSupportsIconsDsl("9.9.9"));
      assertFalse(HermesMavenLocal.gradlePluginSupportsIconsDsl("missing-version"));
    } finally {
      System.setProperty("user.home", previousHome);
    }
  }
}
