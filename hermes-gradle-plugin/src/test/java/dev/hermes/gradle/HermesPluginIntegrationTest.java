package dev.hermes.gradle;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesPluginIntegrationTest {

  private static File hermesRoot;

  @BeforeAll
  static void publishEngineAndPlugin() {
    hermesRoot = locateHermesRoot();
    GradleRunner.create()
        .withProjectDir(hermesRoot)
        .withArguments("publishToMavenLocal", "-q")
        .build();
  }

  @Test
  void hermesRunDesktop_usesJdk17Toolchain() throws IOException {
    String source =
        Files.readString(
            hermesRoot.toPath().resolve("hermes-gradle-plugin/src/main/java/dev/hermes/gradle/HermesPlugin.java"),
            StandardCharsets.UTF_8);
    assertTrue(
        source.contains("JavaLanguageVersion.of(17)"),
        "hermesRunDesktop must use JDK 17 to match Construo export runtime");
  }

  @Test
  void templateProject_onlyGameAndSyncedDesktop(@TempDir Path tempDir) throws IOException {
    Path projectDir = materializeTemplate(tempDir.resolve("demo"));

    BuildResult projects =
        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments("projects", "-q")
            .build();

    String tree = projects.getOutput();
    assertTrue(tree.contains("game"), "game module must be included");
    assertFalse(tree.contains("hermes-api"), "engine API must not be a subproject");
    assertFalse(tree.contains("hermes-core"), "engine core must not be a subproject");

    BuildResult doctor =
        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments(":game:hermesDoctor", "-q")
            .build();
    assertEquals(SUCCESS, doctor.task(":game:hermesDoctor").getOutcome());

    BuildResult compile =
        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments(":game:compileJava", ":hermes-launcher-desktop:compileJava", "-q")
            .build();
    assertEquals(SUCCESS, compile.task(":game:compileJava").getOutcome());
    assertTrue(projectDir.resolve("game/build/classes/java/main").toFile().exists());
    assertTrue(
        projectDir.resolve(".hermes/platforms/hermes-launcher-desktop/build.gradle").toFile().exists(),
        "desktop launcher must be synced under .hermes/platforms");
  }

  @Test
  void templateProject_androidLauncherConfigures(@TempDir Path tempDir) throws IOException {
    Path projectDir = materializeTemplate(tempDir.resolve("android-demo"));
    enableOnlyPlatform(projectDir, "android");

    BuildResult configure =
        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments("projects", "-q")
            .build();
    assertTrue(configure.getOutput().contains("hermes-launcher-android"));
    assertTrue(
        projectDir.resolve(".hermes/platforms/hermes-launcher-android/build.gradle").toFile().exists());
    String launcherBuild =
        Files.readString(
            projectDir.resolve(".hermes/platforms/hermes-launcher-android/build.gradle"),
            StandardCharsets.UTF_8);
    assertTrue(
        launcherBuild.replace("\r\n", "\n").startsWith("buildscript {"),
        "synced Android launcher must use buildscript for AGP resolution");
    assertTrue(
        launcherBuild.contains("com.android.tools.build:gradle:"),
        "synced Android launcher must pin AGP version in buildscript");
    assertFalse(
        launcherBuild.contains("repositories {\n  google()"),
        "synced Android launcher must not declare google-only project repositories");
  }

  @Test
  void templateProject_htmlExportProducesZip(@TempDir Path tempDir) throws IOException {
    Path projectDir = materializeTemplate(tempDir.resolve("html-export"));
    enableOnlyPlatform(projectDir, "html");

    BuildResult export =
        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments(":game:hermesExportHtml", "-q")
            .build();
    assertEquals(SUCCESS, export.task(":game:hermesExportHtml").getOutcome());

    java.nio.file.Path distDir = projectDir.resolve("game/build/dist/html");
    try (var zips = Files.list(distDir).filter(p -> p.toString().endsWith("-html.zip"))) {
      java.nio.file.Path zip = zips.findFirst().orElse(null);
      assertTrue(zip != null && Files.isRegularFile(zip), "HTML export zip must exist under game/build/dist/html");
      assertTrue(Files.size(zip) > 512, "HTML export zip must not be empty");
    }
    assertTrue(
        projectDir.resolve("game/src/main/resources/assets/icons/web/favicon.png").toFile().exists(),
        "template must ship default favicon under assets/icons");
  }

  @Test
  void templateProject_htmlLauncherResolvesGameWithJava11(@TempDir Path tempDir) throws IOException {
    Path projectDir = materializeTemplate(tempDir.resolve("html-demo"));
    enableOnlyPlatform(projectDir, "html");

    BuildResult compile =
        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withPluginClasspath()
            .withArguments(":hermes-launcher-html:compileJava", "-q")
            .build();
    assertEquals(SUCCESS, compile.task(":hermes-launcher-html:compileJava").getOutcome());
  }

  private static void enableOnlyPlatform(Path projectDir, String platform) throws IOException {
    Path settingsFile = projectDir.resolve("settings.gradle");
    String settings = Files.readString(settingsFile, StandardCharsets.UTF_8).replace("\r\n", "\n");
    for (String name : new String[] {"desktop", "html", "android"}) {
      boolean enabled = name.equals(platform);
      settings = settings.replace(name + " { enabled = true", name + " { enabled = " + enabled);
      settings = settings.replace(name + " { enabled = false", name + " { enabled = " + enabled);
    }
    Files.writeString(settingsFile, settings, StandardCharsets.UTF_8);
  }

  private static File locateHermesRoot() {
    Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    if (cwd.getFileName().toString().equals("hermes-gradle-plugin")) {
      return cwd.getParent().toFile();
    }
    return cwd.toFile();
  }

  private static Path materializeTemplate(Path targetDir) throws IOException {
    Path templateRoot = hermesRoot.toPath().resolve("hermes-templates/empty");
    Files.createDirectories(targetDir);
    copySubstitute(templateRoot, targetDir, "Demo", "dev.hermes.demo", "0.1.0-SNAPSHOT");
    String props =
        """
        hermes.engineVersion=0.1.0-SNAPSHOT
        android.useAndroidX=true
        hermes.androidGradlePluginVersion=8.9.3
        """;
    Files.writeString(targetDir.resolve("gradle.properties"), props, StandardCharsets.UTF_8);
    return targetDir;
  }

  private static void copySubstitute(Path source, Path target, String name, String pkg, String version)
      throws IOException {
    Files.walk(source)
        .forEach(
            path -> {
              try {
                Path relative = source.relativize(path);
                String rel = relative.toString().replace('\\', '/');
                rel =
                    rel.replace("{{PROJECT_NAME}}", name)
                        .replace("{{ROOT_PROJECT_NAME}}", name.toLowerCase())
                        .replace("{{package}}", pkg)
                        .replace("{{PACKAGE}}", pkg)
                        .replace("{{APPLICATION_CLASS}}", pkg + ".Game")
                        .replace("{{ENGINE_VERSION}}", version)
                        .replace("{{packageDir}}", pkg.replace('.', '/'))
                        .replace("{{DESKTOP_ENABLED}}", "true")
                        .replace("{{HTML_ENABLED}}", "false")
                        .replace("{{ANDROID_ENABLED}}", "false")
                        .replace("{{ANDROID_APPLICATION_ID}}", pkg);
                Path dest = target.resolve(rel);
                if (Files.isDirectory(path)) {
                  Files.createDirectories(dest);
                } else {
                  Files.createDirectories(dest.getParent());
                  if (isText(path)) {
                    String content = Files.readString(path, StandardCharsets.UTF_8);
                    content =
                        content
                            .replace("{{PROJECT_NAME}}", name)
                            .replace("{{ROOT_PROJECT_NAME}}", name.toLowerCase())
                            .replace("{{package}}", pkg)
                            .replace("{{PACKAGE}}", pkg)
                            .replace("{{APPLICATION_CLASS}}", pkg + ".Game")
                            .replace("{{ENGINE_VERSION}}", version)
                            .replace("{{packageDir}}", pkg.replace('.', '/'))
                            .replace("{{DESKTOP_ENABLED}}", "true")
                            .replace("{{HTML_ENABLED}}", "false")
                            .replace("{{ANDROID_ENABLED}}", "false")
                            .replace("{{ANDROID_APPLICATION_ID}}", pkg);
                    Files.writeString(dest, content, StandardCharsets.UTF_8);
                  } else {
                    Files.copy(path, dest);
                  }
                }
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
    Path gradlew = target.resolve("gradlew");
    if (Files.isRegularFile(gradlew)) {
      gradlew.toFile().setExecutable(true);
    }
  }

  private static boolean isText(Path path) {
    String pathString = path.toString().replace('\\', '/');
    if (pathString.contains("META-INF/services/")) {
      return true;
    }
    String n = path.getFileName().toString();
    return n.endsWith(".gradle")
        || n.endsWith(".java")
        || n.endsWith(".json")
        || n.endsWith(".properties")
        || n.endsWith(".md")
        || n.equals("gradlew")
        || n.endsWith(".bat");
  }
}
