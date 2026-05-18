package dev.hermes.gradle;

import dev.hermes.tooling.AndroidSdkValidator;
import dev.hermes.tooling.HermesDoctorSupport;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;

/** Validates Hermes projects and environment; Gradle task and CLI entry points. */
public final class HermesDoctor {

  private HermesDoctor() {}

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

  public static void runGradle(Project gameProject) {
    java.util.List<CheckResult> results = new java.util.ArrayList<>();
    results.add(checkJdk());
    results.add(checkHermesJson(gameProject.file("hermes.json")));
    results.add(checkEngineResolution(gameProject));
    results.addAll(checkForbiddenImports(gameProject));
    results.addAll(checkPlatforms(gameProject));
    results.add(checkWritableDirs(gameProject));
    printAndThrow(results);
  }

  public static java.util.List<CheckResult> runStandalone(java.nio.file.Path projectDir) {
    java.util.List<CheckResult> results = new java.util.ArrayList<>();
    for (HermesDoctorSupport.CheckResult support : HermesDoctorSupport.runStandalone(projectDir)) {
      results.add(fromSupport(support));
    }
    return results;
  }

  public static void printResults(java.util.List<CheckResult> results) {
    for (CheckResult result : results) {
      System.out.println("[" + result.status() + "] " + result.name() + ": " + result.message());
      if (result.fix() != null && !result.fix().isBlank() && result.status() != Status.OK) {
        System.out.println("       fix: " + result.fix());
      }
    }
  }

  public static boolean hasFailure(java.util.List<CheckResult> results) {
    for (CheckResult result : results) {
      if (result.status() == Status.FAIL) {
        return true;
      }
    }
    return false;
  }

  public static void printAndThrow(java.util.List<CheckResult> results) {
    printResults(results);
    if (hasFailure(results)) {
      throw new GradleException("hermesDoctor found failures (see above).");
    }
  }

  static java.util.List<String> findForbiddenImports(java.nio.file.Path srcRoot) {
    return HermesDoctorSupport.findForbiddenImports(srcRoot);
  }

  private static CheckResult fromSupport(HermesDoctorSupport.CheckResult support) {
    return new CheckResult(
        support.name(),
        Status.valueOf(support.status().name()),
        support.message(),
        support.fix());
  }

  private static CheckResult checkJdk() {
    JavaVersion current = JavaVersion.current();
    if (current.isCompatibleWith(JavaVersion.VERSION_11)) {
      return new CheckResult("jdk", Status.OK, "JDK " + current + " is OK.", null);
    }
    return new CheckResult(
        "jdk",
        Status.FAIL,
        "JDK " + current + " is too old (need 11+).",
        "Install JDK 11 or newer and set JAVA_HOME.");
  }

  private static CheckResult checkHermesJson(File file) {
    if (file == null || !file.isFile()) {
      return new CheckResult(
          "hermes.json",
          Status.WARN,
          "hermes.json not found at " + (file == null ? "?" : file.getAbsolutePath()),
          "Add game/hermes.json with name, version, and scene path.");
    }
    try {
      HermesGameConfigParser.parse(file);
      return new CheckResult("hermes.json", Status.OK, "Valid: " + file.getAbsolutePath(), null);
    } catch (GradleException e) {
      return new CheckResult("hermes.json", Status.FAIL, e.getMessage(), "Fix JSON syntax and required fields.");
    }
  }

  private static CheckResult checkEngineResolution(Project gameProject) {
    HermesExtension extension = gameProject.getExtensions().findByType(HermesExtension.class);
    if (gameProject.findProject(":hermes-api") != null) {
      return new CheckResult("engine", Status.OK, "Using sibling projects hermes-api / hermes-core.", null);
    }
    File home = HermesHomeResolver.resolve(gameProject);
    if (HermesHomeResolver.isHermesCheckout(home)) {
      return new CheckResult("engine", Status.OK, "HERMES_HOME engine checkout: " + home.getAbsolutePath(), null);
    }
    String version =
        extension != null
            ? HermesEngineVersion.resolve(gameProject, extension)
            : gameProject.getVersion().toString();
    File artifact =
        new File(
            System.getProperty("user.home")
                + "/.m2/repository/dev/hermes/hermes-api/"
                + version
                + "/hermes-api-"
                + version
                + ".jar");
    if (artifact.isFile()) {
      return new CheckResult("engine", Status.OK, "Maven local hermes-api " + version + " found.", null);
    }
    return new CheckResult(
        "engine",
        Status.FAIL,
        "No hermes-api in build and not in Maven local (" + artifact.getAbsolutePath() + ").",
        "Run ./gradlew publishToMavenLocal from Hermes, set HERMES_HOME, or include engine modules.");
  }

  private static java.util.List<CheckResult> checkForbiddenImports(Project gameProject) {
    java.util.List<CheckResult> results = new java.util.ArrayList<>();
    File srcRoot = gameProject.file("src/main/java");
    if (!srcRoot.isDirectory()) {
      return results;
    }
    java.util.List<String> violations = findForbiddenImports(srcRoot.toPath());
    if (violations.isEmpty()) {
      results.add(
          new CheckResult(
              "libgdx-imports",
              Status.OK,
              "No com.badlogicgames.gdx imports in " + gameProject.getPath() + "/src",
              null));
    } else {
      results.add(
          new CheckResult(
              "libgdx-imports",
              Status.FAIL,
              "Forbidden libGDX imports: " + String.join(", ", violations),
              "Use hermes-api types only; libGDX stays inside hermes-core."));
    }
    return results;
  }

  private static java.util.List<CheckResult> checkPlatforms(Project gameProject) {
    java.util.List<CheckResult> results = new java.util.ArrayList<>();
    Project root = gameProject.getRootProject();
    if (root.findProject(":hermes-launcher-android") != null) {
      File sdk = resolveAndroidSdk(root);
      if (!AndroidSdkValidator.isValidSdk(sdk)) {
        results.add(
            new CheckResult(
                "android-sdk",
                Status.FAIL,
                "Android launcher is included but SDK not found.",
                "Set sdk.dir in local.properties, hermes.android.sdk, or ANDROID_SDK_ROOT."));
      } else {
        results.add(new CheckResult("android-sdk", Status.OK, "SDK: " + sdk.getAbsolutePath(), null));
      }
    }
    return results;
  }

  private static File resolveAndroidSdk(Project root) {
    File localProperties = root.file("local.properties");
    if (localProperties.isFile()) {
      try {
        for (String line : Files.readAllLines(localProperties.toPath(), StandardCharsets.UTF_8)) {
          if (line.startsWith("sdk.dir=")) {
            String path = line.substring("sdk.dir=".length()).trim();
            if (!path.isBlank()) {
              File sdk = new File(path);
              if (AndroidSdkValidator.isValidSdk(sdk)) {
                return sdk;
              }
            }
          }
        }
      } catch (IOException ignored) {
        // fall through
      }
    }
    String fromEnv = System.getenv("ANDROID_SDK_ROOT");
    if (fromEnv == null || fromEnv.isBlank()) {
      fromEnv = System.getenv("ANDROID_HOME");
    }
    if (fromEnv != null && !fromEnv.isBlank()) {
      File sdk = new File(fromEnv);
      if (AndroidSdkValidator.isValidSdk(sdk)) {
        return sdk;
      }
    }
    return null;
  }

  private static CheckResult checkWritableDirs(Project gameProject) {
    File buildDir = gameProject.getLayout().getBuildDirectory().getAsFile().get();
    File gradleDir = gameProject.getRootProject().file(".gradle");
    try {
      if (!buildDir.exists()) {
        Files.createDirectories(buildDir.toPath());
      }
      if (!gradleDir.exists()) {
        Files.createDirectories(gradleDir.toPath());
      }
      return new CheckResult("writable-dirs", Status.OK, "build/ and .gradle/ are writable.", null);
    } catch (IOException e) {
      return new CheckResult(
          "writable-dirs",
          Status.FAIL,
          "Cannot create build directories: " + e.getMessage(),
          "Check directory permissions for " + gameProject.getRootDir());
    }
  }
}
