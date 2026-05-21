package dev.hermes.gradle;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Verifies smoke-mode {@code hermesRunDesktop} wiring via headless launcher (no GLFW/display). */
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
  void hermesRunDesktop_smokeFramesExitsSuccessfully() {
    BuildResult result =
        GradleRunner.create()
            .withProjectDir(hermesRoot)
            .withArguments(":game:hermesRunDesktop", "-Phermes.desktop.smokeFrames=2", "-q")
            .build();

    assertEquals(SUCCESS, result.task(":game:hermesRunDesktop").getOutcome());
  }

  private static File locateHermesRoot() {
    Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    if (cwd.getFileName().toString().equals("hermes-gradle-plugin")) {
      return cwd.getParent().toFile();
    }
    return cwd.toFile();
  }
}
