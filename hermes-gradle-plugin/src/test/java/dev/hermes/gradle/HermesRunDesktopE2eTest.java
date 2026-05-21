package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Verifies {@code hermesRunDesktop} task wiring without launching GLFW (dry-run). */
class HermesRunDesktopE2eTest {

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
  void hermesRunDesktop_dryRunSucceeds() {
    BuildResult result =
        GradleRunner.create()
            .withProjectDir(hermesRoot)
            .withArguments(":game:hermesRunDesktop", "--dry-run")
            .build();

    assertTrue(
        result.getOutput().contains("hermesRunDesktop"),
        "dry-run should list :game:hermesRunDesktop");
  }

  private static File locateHermesRoot() {
    Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    if (cwd.getFileName().toString().equals("hermes-gradle-plugin")) {
      return cwd.getParent().toFile();
    }
    return cwd.toFile();
  }
}
