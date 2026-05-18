package dev.hermes.tooling;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Environment and source checks shared by Gradle {@code hermesDoctor} and the CLI. */
public final class HermesDoctorSupport {

  private static final Pattern FORBIDDEN_IMPORT =
      Pattern.compile("^\\s*import\\s+com\\.badlogicgames\\.gdx\\b.*");

  public enum Status {
    OK,
    WARN,
    FAIL
  }

  public static final class CheckResult {
    private final String name;
    private final Status status;
    private final String message;
    private final String fix;

    public CheckResult(String name, Status status, String message, String fix) {
      this.name = name;
      this.status = status;
      this.message = message;
      this.fix = fix;
    }

    public String name() {
      return name;
    }

    public Status status() {
      return status;
    }

    public String message() {
      return message;
    }

    public String fix() {
      return fix;
    }
  }

  private HermesDoctorSupport() {}

  public static List<CheckResult> runStandalone(Path projectDir) {
    List<CheckResult> results = new ArrayList<>();
    results.add(checkJdk());
    results.add(checkHermesHomeEnv());
    results.add(checkMavenLocalArtifacts(projectDir));
    if (projectDir != null) {
      results.add(checkHermesJson(projectDir.resolve("game/hermes.json").toFile()));
      if (!Files.isDirectory(projectDir.resolve("game"))) {
        results.add(
            new CheckResult(
                "project-layout",
                Status.FAIL,
                "Missing game/ module in " + projectDir,
                "Run hermes new or ensure the project contains a game/ directory."));
      }
    }
    return results;
  }

  public static boolean hasFailure(List<CheckResult> results) {
    for (CheckResult result : results) {
      if (result.status() == Status.FAIL) {
        return true;
      }
    }
    return false;
  }

  public static void printResults(List<CheckResult> results) {
    for (CheckResult result : results) {
      System.out.println("[" + result.status() + "] " + result.name() + ": " + result.message());
      if (result.fix() != null && !result.fix().isBlank() && result.status() != Status.OK) {
        System.out.println("       fix: " + result.fix());
      }
    }
  }

  public static List<String> findForbiddenImports(Path srcRoot) {
    List<String> violations = new ArrayList<>();
    if (!Files.isDirectory(srcRoot)) {
      return violations;
    }
    try {
      Files.walk(srcRoot)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(
              file -> {
                try {
                  List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                  for (int i = 0; i < lines.size(); i++) {
                    if (FORBIDDEN_IMPORT.matcher(lines.get(i)).matches()) {
                      violations.add(
                          srcRoot.relativize(file).toString().replace('\\', '/')
                              + ":"
                              + (i + 1));
                    }
                  }
                } catch (IOException e) {
                  violations.add("scan-error: " + e.getMessage());
                }
              });
    } catch (IOException e) {
      violations.add("scan-error: " + e.getMessage());
    }
    return violations;
  }

  private static CheckResult checkJdk() {
    String version = System.getProperty("java.version", "unknown");
    int major = Runtime.version().feature();
    if (major >= 11) {
      return new CheckResult("jdk", Status.OK, "JDK " + version + " is OK.", null);
    }
    return new CheckResult(
        "jdk",
        Status.FAIL,
        "JDK " + version + " is too old (need 11+).",
        "Install JDK 11 or newer and set JAVA_HOME.");
  }

  private static CheckResult checkHermesJson(File file) {
    if (file == null || !file.isFile()) {
      return new CheckResult(
          "hermes.json",
          Status.WARN,
          "hermes.json not found",
          "Add game/hermes.json with title and scene path.");
    }
    try {
      HermesGameConfigParser.parse(file);
      return new CheckResult("hermes.json", Status.OK, "Valid: " + file.getAbsolutePath(), null);
    } catch (HermesConfigException e) {
      return new CheckResult("hermes.json", Status.FAIL, e.getMessage(), "Fix JSON syntax and fields.");
    }
  }

  private static CheckResult checkHermesHomeEnv() {
    String home = System.getenv(HermesHomeResolver.ENV_HERMES_HOME);
    if (home == null || home.isBlank()) {
      return new CheckResult(
          "HERMES_HOME",
          Status.WARN,
          "HERMES_HOME is not set (optional if Maven local has engine artifacts).",
          "Export HERMES_HOME=/path/to/hermes for composite engine resolution.");
    }
    File dir = new File(home);
    if (HermesHomeResolver.isHermesCheckout(dir)) {
      return new CheckResult("HERMES_HOME", Status.OK, "Valid checkout: " + dir.getAbsolutePath(), null);
    }
    return new CheckResult(
        "HERMES_HOME",
        Status.FAIL,
        "HERMES_HOME does not look like a Hermes checkout: " + home,
        "Point HERMES_HOME at the engine repo root.");
  }

  private static CheckResult checkMavenLocalArtifacts(Path projectDir) {
    String version = "0.1.0-SNAPSHOT";
    if (projectDir != null) {
      Path gradleProps = projectDir.resolve("gradle.properties");
      if (Files.isRegularFile(gradleProps)) {
        try {
          for (String line : Files.readAllLines(gradleProps, StandardCharsets.UTF_8)) {
            if (line.startsWith("hermes.engineVersion=")) {
              version = line.substring("hermes.engineVersion=".length()).trim();
              break;
            }
          }
        } catch (IOException ignored) {
          // keep default
        }
      }
    }
    File apiJar =
        Path.of(
                System.getProperty("user.home"),
                ".m2/repository/dev/hermes/hermes-api",
                version,
                "hermes-api-" + version + ".jar")
            .toFile();
    if (!apiJar.isFile()) {
      return new CheckResult(
          "maven-local",
          Status.WARN,
          "hermes-api not in Maven local for version " + version,
          "From Hermes engine repo run: ./gradlew publishToMavenLocal");
    }
    if (!HermesMavenLocal.gradlePluginSupportsIconsDsl(version)) {
      return new CheckResult(
          "hermes-gradle-plugin",
          Status.FAIL,
          "Published plugin is older than this project (missing icons DSL).",
          "From Hermes engine repo run: ./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal");
    }
    return new CheckResult("maven-local", Status.OK, "Found " + apiJar.getName(), null);
  }
}
