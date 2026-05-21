package dev.hermes.gradle.doctor;

import dev.hermes.gradle.HermesConfig;
import dev.hermes.gradle.HermesHomeGradle;
import dev.hermes.tooling.android.AndroidSdkLocator;
import dev.hermes.tooling.config.HermesConfigException;
import dev.hermes.tooling.config.HermesGameConfigParser;
import dev.hermes.tooling.doctor.HermesDoctorSupport;
import dev.hermes.tooling.doctor.HermesDoctorSupport.CheckResult;
import dev.hermes.tooling.doctor.HermesDoctorSupport.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;

/** Validates Hermes projects and environment; Gradle {@code hermesDoctor} task entry point. */
public final class HermesDoctor {

  private HermesDoctor() {}

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

  private static void printAndThrow(java.util.List<CheckResult> results) {
    HermesDoctorSupport.printResults(results);
    if (HermesDoctorSupport.hasFailure(results)) {
      throw new GradleException("hermesDoctor found failures (see above).");
    }
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
          "Add game/hermes.json with title and scene path.");
    }
    try {
      HermesGameConfigParser.parse(file);
      return new CheckResult("hermes.json", Status.OK, "Valid: " + file.getAbsolutePath(), null);
    } catch (HermesConfigException e) {
      return new CheckResult("hermes.json", Status.FAIL, e.getMessage(), "Fix JSON syntax and required fields.");
    }
  }

  private static CheckResult checkEngineResolution(Project gameProject) {
    if (gameProject.findProject(":hermes-api") != null) {
      return new CheckResult("engine", Status.OK, "Using sibling projects hermes-api / hermes-core.", null);
    }
    File home = HermesHomeGradle.resolve(gameProject);
    if (HermesHomeGradle.isHermesCheckout(home)) {
      return new CheckResult("engine", Status.OK, "HERMES_HOME engine checkout: " + home.getAbsolutePath(), null);
    }
    String version = HermesConfig.resolveEngineVersion(gameProject);
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
    java.util.List<String> violations = HermesDoctorSupport.findForbiddenImports(srcRoot.toPath());
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
      File sdk = AndroidSdkLocator.locate(root.getRootDir());
      if (sdk == null) {
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
