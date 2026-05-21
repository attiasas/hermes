package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.gradle.platform.HermesPlatformSync;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesPlatformSyncTest {

  @Test
  void syncIfNeeded_rendersAndroidBuildscriptFromTemplate(@TempDir Path tempDir) throws Exception {
    Path root = tempDir.resolve("project");
    Path launcher = root.resolve(".hermes/platforms/hermes-launcher-android");
    Files.createDirectories(launcher);
    Files.writeString(
        launcher.resolve("build.gradle"),
        "apply plugin: 'com.android.application'\r\n\r\ntasks.named('preBuild')\r\n",
        StandardCharsets.UTF_8);
    Files.writeString(
        root.resolve("gradle.properties"),
        "hermes.androidGradlePluginVersion=8.9.3\r\n",
        StandardCharsets.UTF_8);

    HermesPlatformSync.syncIfNeeded(root.toFile(), "hermes-launcher-android", "0.1.0-SNAPSHOT", null);

    String buildGradle = Files.readString(launcher.resolve("build.gradle"), StandardCharsets.UTF_8);
    assertTrue(
        buildGradle.startsWith("buildscript {"),
        "sync must render Android buildscript from template");
    assertTrue(buildGradle.contains("com.android.tools.build:gradle:8.9.3"));
    assertTrue(buildGradle.contains("implementation 'dev.hermes:hermes-core:0.1.0-SNAPSHOT'"));
    assertFalse(buildGradle.startsWith("apply plugin:"));
    assertFalse(buildGradle.contains("repositories {\n  google()\n}"));
  }
}
