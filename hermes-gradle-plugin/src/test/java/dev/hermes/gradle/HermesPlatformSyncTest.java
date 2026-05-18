package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesPlatformSyncTest {

  @Test
  void syncIfNeeded_patchesCrlfAndroidApplyPlugin(@TempDir Path tempDir) throws Exception {
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
        "CRLF apply plugin must be converted to buildscript block");
    assertTrue(buildGradle.contains("com.android.tools.build:gradle:8.9.3"));
    assertFalse(buildGradle.startsWith("apply plugin:"));
  }

  @Test
  void syncIfNeeded_patchesDesktopConstruoJdkRootToLazyProvider(@TempDir Path tempDir) throws Exception {
    Path root = tempDir.resolve("project");
    Path launcher = root.resolve(".hermes/platforms/hermes-launcher-desktop");
    Files.createDirectories(launcher);
    String eager =
        """
        tasks.withType(CreateRuntimeImageTask).configureEach { CreateRuntimeImageTask task ->
          String target = task.name.substring('createRuntimeImage'.length())
          String targetKey = target.substring(0, 1).toLowerCase(Locale.ROOT) + target.substring(1)
          task.dependsOn("unzipJdk${target}")
          task.jdkRoot.set(
              layout.buildDirectory.dir("construo/jdk/${targetKey}").map { dir ->
                def jlink =
                    fileTree(dir.asFile).matching { include '**/bin/jlink' }.files.find { it?.isFile() }
                if (jlink == null) {
                  throw new GradleException("No jlink under ${dir.asFile} (run unzipJdk${target} first)")
                }
                layout.projectDirectory.dir(jlink.parentFile.parentFile.absolutePath)
              })
        }
        """;
    Files.writeString(launcher.resolve("build.gradle"), eager, StandardCharsets.UTF_8);

    HermesPlatformSync.syncIfNeeded(root.toFile(), "hermes-launcher-desktop", "0.1.0-SNAPSHOT", null);

    String buildGradle = Files.readString(launcher.resolve("build.gradle"), StandardCharsets.UTF_8);
    assertTrue(buildGradle.contains("tasks.named(\"unzipJdk${target}\").map {"), buildGradle);
    assertTrue(buildGradle.contains("downloadJdk${target}"));
    assertTrue(buildGradle.contains("layout.projectDirectory.dir(jlink.parentFile.parentFile.absolutePath)"));
    assertFalse(buildGradle.contains(".map { dir ->"));
  }
}
