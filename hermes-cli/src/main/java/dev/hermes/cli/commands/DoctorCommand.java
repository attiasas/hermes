package dev.hermes.cli.commands;

import dev.hermes.tooling.doctor.HermesDoctorSupport;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "doctor", description = "Validate environment and Hermes project setup")
public final class DoctorCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Project directory (default: .)")
    Path projectDir = Path.of(".");

    @Option(names = "--no-gradle", description = "Skip delegating to ./gradlew hermesDoctor")
    boolean noGradle;

    @Override
    public void run() {
        Path root = projectDir.toAbsolutePath().normalize();
        if (isGradleProject(root) && !noGradle) {
            int exit = runGradleDoctor(root);
            if (exit >= 0) {
                System.exit(exit);
            }
        }
        List<HermesDoctorSupport.CheckResult> results = new ArrayList<>();
        results.addAll(HermesDoctorSupport.runStandalone(root));
        if (isGradleProject(root) && noGradle) {
            results.add(
                    new HermesDoctorSupport.CheckResult(
                            "gradle-delegate",
                            HermesDoctorSupport.Status.WARN,
                            "Gradle project detected; run ./gradlew :game:hermesDoctor for full checks.",
                            null));
        }
        HermesDoctorSupport.printResults(results);
        if (HermesDoctorSupport.hasFailure(results)) {
            throw new picocli.CommandLine.ExecutionException(
                    new picocli.CommandLine(DoctorCommand.class), "hermes doctor found failures");
        }
    }

    private static boolean isGradleProject(Path root) {
        return Files.isRegularFile(root.resolve("settings.gradle"))
                || Files.isRegularFile(root.resolve("settings.gradle.kts"));
    }

    private static int runGradleDoctor(Path root) {
        File gradlew = root.resolve("gradlew").toFile();
        if (!gradlew.isFile()) {
            System.err.println("settings.gradle found but gradlew is missing; running standalone checks.");
            return -1;
        }
        ProcessBuilder builder =
                new ProcessBuilder(
                        gradlew.getAbsolutePath(),
                        ":game:hermesDoctor",
                        "--no-daemon",
                        "--stacktrace");
        builder.directory(root.toFile());
        builder.inheritIO();
        try {
            Process process = builder.start();
            boolean finished = process.waitFor(10, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("hermesDoctor timed out.");
                return 1;
            }
            return process.exitValue();
        } catch (Exception e) {
            System.err.println("Failed to run Gradle hermesDoctor: " + e.getMessage());
            return 1;
        }
    }
}
