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
        gdxVersion=1.14.0
        lwjgl3Version=3.4.1
        gdxTeaVMVersion=1.5.5
        android.useAndroidX=true
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
                        .replace("{{packageDir}}", pkg.replace('.', '/'));
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
                            .replace("{{packageDir}}", pkg.replace('.', '/'));
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
