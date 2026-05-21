package dev.hermes.cli;

import dev.hermes.tooling.HermesHomeResolver;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "studio", description = "Open Hermes Studio for a project directory")
public final class StudioCommand implements Runnable {

  static final String STUDIO_JAR_NAME = "hermes-studio.jar";

  @Parameters(index = "0", arity = "0..1", defaultValue = ".", description = "Project directory")
  File directory;

  @Override
  public void run() {
    Path projectDir = directory.toPath().toAbsolutePath().normalize();
    int exit = launch(projectDir);
    System.exit(exit);
  }

  static int launch(Path projectDir) {
    Path engineRoot = resolveEngineRoot(projectDir);
    if (engineRoot != null) {
      Path gradlew = engineRoot.resolve("gradlew");
      if (Files.isRegularFile(gradlew)) {
        return runGradleStudio(engineRoot, projectDir);
      }
      Path studioJar = resolveStudioJar(engineRoot);
      if (studioJar != null) {
        return runJavaJar(studioJar, projectDir);
      }
    }

    printLaunchFailure(engineRoot);
    return 1;
  }

  static Path resolveEngineRoot(Path projectDir) {
    File fromResolver = HermesHomeResolver.resolve(projectDir);
    if (fromResolver != null && HermesHomeResolver.isHermesCheckout(fromResolver)) {
      return fromResolver.toPath();
    }
    Path walk = projectDir;
    while (walk != null) {
      if (HermesHomeResolver.isHermesCheckout(walk.toFile())) {
        return walk;
      }
      walk = walk.getParent();
    }
    return null;
  }

  static Path resolveStudioJar(Path engineRoot) {
    Path bundled = engineRoot.resolve("studio").resolve(STUDIO_JAR_NAME);
    return Files.isRegularFile(bundled) ? bundled : null;
  }

  private static int runGradleStudio(Path engineRoot, Path projectDir) {
    File gradlew = engineRoot.resolve("gradlew").toFile();
    String args = "--project " + projectDir.toAbsolutePath().normalize();
    ProcessBuilder builder =
        new ProcessBuilder(
            gradlew.getAbsolutePath(),
            ":hermes-studio-app:run",
            "--args=" + args,
            "--no-daemon");
    builder.directory(engineRoot.toFile());
    builder.inheritIO();
    return waitForProcess(builder, "Gradle :hermes-studio-app:run");
  }

  private static int runJavaJar(Path jar, Path projectDir) {
    ProcessBuilder builder =
        new ProcessBuilder(
            "java",
            "-jar",
            jar.toAbsolutePath().toString(),
            "--project",
            projectDir.toAbsolutePath().normalize().toString());
    builder.inheritIO();
    return waitForProcess(builder, "java -jar hermes-studio");
  }

  private static int waitForProcess(ProcessBuilder builder, String label) {
    try {
      Process process = builder.start();
      boolean finished = process.waitFor(30, TimeUnit.MINUTES);
      if (!finished) {
        process.destroyForcibly();
        System.err.println(label + " timed out.");
        return 1;
      }
      return process.exitValue();
    } catch (Exception e) {
      System.err.println("Failed to start Hermes Studio (" + label + "): " + e.getMessage());
      return 1;
    }
  }

  private static void printLaunchFailure(Path engineRoot) {
    System.err.println("Could not start Hermes Studio.");
    if (engineRoot == null) {
      System.err.println(
          "Set HERMES_HOME to your Hermes engine checkout (or run from inside the engine repo).");
      System.err.println(
          "Example: export HERMES_HOME=/path/to/hermes && hermes studio /path/to/my-game");
      return;
    }
    System.err.println("Engine root: " + engineRoot.toAbsolutePath());
    System.err.println(
        "Expected "
            + engineRoot.resolve("gradlew").toAbsolutePath()
            + " (run ./gradlew :hermes-studio-app:installDist from the engine repo),");
    System.err.println(
        "or place a fat jar at "
            + engineRoot.resolve("studio").resolve(STUDIO_JAR_NAME).toAbsolutePath()
            + ".");
  }
}
