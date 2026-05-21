package dev.hermes.gradle.platform;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlatformTemplateRendererTest {

  @Test
  void render_android_usesMavenCoordsAndAgpBuildscript() {
    PlatformSyncContext context =
        new PlatformSyncContext("hermes-launcher-android", "1.2.3", "8.9.3");
    String buildGradle = PlatformTemplateRenderer.render("hermes-launcher-android", context);

    assertTrue(buildGradle.startsWith("buildscript {"));
    assertTrue(buildGradle.contains("com.android.tools.build:gradle:8.9.3"));
    assertTrue(buildGradle.contains("implementation 'dev.hermes:hermes-core:1.2.3'"));
    assertTrue(buildGradle.contains("implementation project(':game')"));
    assertFalse(buildGradle.contains("repositories {\n  google()\n}"));
    assertFalse(buildGradle.contains("project(':hermes-core')"));
  }

  @Test
  void render_html_usesCompileOnlyGame() {
    PlatformSyncContext context =
        new PlatformSyncContext("hermes-launcher-html", "0.1.0-SNAPSHOT", "8.9.3");
    String buildGradle = PlatformTemplateRenderer.render("hermes-launcher-html", context);

    assertTrue(buildGradle.contains("compileOnly project(':game')"));
    assertTrue(buildGradle.contains("implementation 'dev.hermes:hermes-core:0.1.0-SNAPSHOT'"));
    assertTrue(buildGradle.contains("JavaLanguageVersion.of(11)"));
  }

  @Test
  void render_desktop_usesJava17ToolchainAndRelease11() {
    PlatformSyncContext context =
        new PlatformSyncContext("hermes-launcher-desktop", "0.1.0-SNAPSHOT", "8.9.3");
    String buildGradle = PlatformTemplateRenderer.render("hermes-launcher-desktop", context);

    assertTrue(buildGradle.contains("JavaLanguageVersion.of(17)"));
    assertTrue(buildGradle.contains("options.release = 11"));
    assertTrue(buildGradle.contains("implementation project(':game')"));
    assertFalse(buildGradle.contains("--enable-native-access"));
  }
}
