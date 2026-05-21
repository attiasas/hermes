package dev.hermes.tooling.doctor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.hermes.tooling.config.HermesConfigException;
import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.config.HermesGameConfigParser;
import dev.hermes.tooling.gradle.HermesHomeResolver;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Environment and source checks shared by Gradle {@code hermesDoctor} and the CLI. */
public final class HermesDoctorSupport {

  private static final Pattern FORBIDDEN_IMPORT =
      Pattern.compile("^\\s*import\\s+com\\.badlogicgames\\.gdx\\b.*");

  private static final Set<String> BUILTIN_SHADER_FILES =
      Set.of("default.vert", "default.frag");

  private static final Set<String> BUILTIN_SHADER_PATHS =
      Set.of("shaders/default.vert", "shaders/default.frag");

  private static final Gson GSON = new Gson();

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

  /**
   * Fails when the HTML platform is enabled and a referenced render pipeline JSON declares
   * non-builtin shader paths (only {@code shaders/default.vert} and {@code shaders/default.frag}
   * are allowed for TeaVM v1).
   */
  public static CheckResult checkHtmlCustomShaders(Path projectRoot) {
    if (projectRoot == null || !Files.isDirectory(projectRoot)) {
      return new CheckResult(
          "html-custom-shaders", Status.OK, "No project root; skipped HTML shader check.", null);
    }
    if (!isHtmlPlatformEnabled(projectRoot)) {
      return new CheckResult(
          "html-custom-shaders",
          Status.OK,
          "HTML platform disabled; custom GLSL allowed for desktop.",
          null);
    }
    List<String> nonBuiltin = findNonBuiltinShaderPathsInReferencedPipelines(projectRoot);
    if (nonBuiltin.isEmpty()) {
      return new CheckResult(
          "html-custom-shaders",
          Status.OK,
          "HTML enabled; referenced pipelines use builtin shaders only.",
          null);
    }
    return new CheckResult(
        "html-custom-shaders",
        Status.FAIL,
        "HTML platform is enabled but referenced pipelines declare custom GLSL: "
            + String.join(", ", nonBuiltin),
        "Disable HTML in settings.gradle (hermes { platforms { html { enabled = false } } }), "
            + "or use only shaders/default.vert and shaders/default.frag in every referenced "
            + "render pipeline (TeaVM supports builtin shaders only in v1).");
  }

  static List<String> findNonBuiltinShaderPathsInReferencedPipelines(Path projectRoot) {
    Path assets = projectRoot.resolve("game/src/main/resources/assets");
    Set<String> pipelinePaths = collectReferencedPipelinePaths(projectRoot);
    List<String> violations = new ArrayList<>();
    for (String relative : pipelinePaths) {
      Path pipelineFile = assets.resolve(relative);
      if (!Files.isRegularFile(pipelineFile)) {
        continue;
      }
      collectNonBuiltinShaderPaths(pipelineFile, violations);
    }
    return violations.stream().distinct().sorted().collect(Collectors.toList());
  }

  static Set<String> collectReferencedPipelinePaths(Path projectRoot) {
    LinkedHashSet<String> paths = new LinkedHashSet<>();
    Path hermesJson = projectRoot.resolve("game/hermes.json");
    if (Files.isRegularFile(hermesJson)) {
      try {
        HermesGameConfig config = HermesGameConfigParser.parse(hermesJson.toFile());
        paths.add(config.getRenderPipeline());
      } catch (HermesConfigException ignored) {
        // hermes.json validity is reported by a separate check
      }
    }
    Path scenesDir = projectRoot.resolve("game/src/main/resources/assets/scenes");
    if (Files.isDirectory(scenesDir)) {
      try (Stream<Path> sceneFiles = Files.list(scenesDir)) {
        sceneFiles
            .filter(Files::isRegularFile)
            .filter(path -> path.getFileName().toString().endsWith(".json"))
            .forEach(scene -> addScenePipelineOverride(scene, paths));
      } catch (IOException e) {
        paths.add("scan-error:scenes:" + e.getMessage());
      }
    }
    return paths;
  }

  private static void addScenePipelineOverride(Path sceneFile, Set<String> paths) {
    try {
      JsonObject root = GSON.fromJson(Files.readString(sceneFile, StandardCharsets.UTF_8), JsonObject.class);
      if (root == null || !root.has("renderPipeline")) {
        return;
      }
      String renderPipeline = root.get("renderPipeline").getAsString().trim();
      if (!renderPipeline.isEmpty()) {
        paths.add(renderPipeline);
      }
    } catch (IOException | JsonParseException | IllegalStateException ignored) {
      // invalid scene JSON is handled elsewhere
    }
  }

  private static void collectNonBuiltinShaderPaths(Path pipelineFile, List<String> violations) {
    try {
      JsonObject root =
          GSON.fromJson(Files.readString(pipelineFile, StandardCharsets.UTF_8), JsonObject.class);
      if (root == null || !root.has("shaders")) {
        return;
      }
      JsonObject shaders = root.getAsJsonObject("shaders");
      for (Map.Entry<String, JsonElement> entry : shaders.entrySet()) {
        if (!entry.getValue().isJsonObject()) {
          continue;
        }
        JsonObject def = entry.getValue().getAsJsonObject();
        if (def.has("vertex")) {
          noteShaderPath(def.get("vertex").getAsString(), violations);
        }
        if (def.has("fragment")) {
          noteShaderPath(def.get("fragment").getAsString(), violations);
        }
      }
    } catch (IOException e) {
      violations.add("scan-error:" + pipelineFile.getFileName() + ":" + e.getMessage());
    } catch (JsonParseException | IllegalStateException e) {
      violations.add("scan-error:" + pipelineFile.getFileName() + ":" + e.getMessage());
    }
  }

  private static void noteShaderPath(String path, List<String> violations) {
    if (path == null || path.isBlank()) {
      return;
    }
    String normalized = path.trim();
    if (!BUILTIN_SHADER_PATHS.contains(normalized)) {
      violations.add(normalized);
    }
  }

  static boolean isHtmlPlatformEnabled(Path projectRoot) {
    Path settings = projectRoot.resolve("settings.gradle");
    if (!Files.isRegularFile(settings)) {
      return false;
    }
    try {
      String content = Files.readString(settings);
      if (content.contains("hermes-launcher-html")) {
        return true;
      }
      return parseHtmlEnabledFromHermesBlock(content);
    } catch (IOException e) {
      return false;
    }
  }

  private static boolean parseHtmlEnabledFromHermesBlock(String settingsContent) {
    String lower = settingsContent.toLowerCase(Locale.ROOT);
    if (lower.contains("html { enabled = false") || lower.contains("html {enabled = false")) {
      return false;
    }
    return lower.contains("html { enabled = true")
        || lower.contains("html {enabled = true")
        || lower.contains("html { enabled=true")
        || lower.contains("html {enabled=true");
  }

  static List<String> findCustomShaderFiles(Path shadersDir) {
    List<String> custom = new ArrayList<>();
    if (!Files.isDirectory(shadersDir)) {
      return custom;
    }
    try (Stream<Path> paths = Files.list(shadersDir)) {
      paths
          .filter(Files::isRegularFile)
          .map(path -> path.getFileName().toString())
          .filter(name -> name.endsWith(".vert") || name.endsWith(".frag"))
          .filter(name -> !BUILTIN_SHADER_FILES.contains(name))
          .sorted()
          .forEach(name -> custom.add("assets/shaders/" + name));
    } catch (IOException e) {
      custom.add("scan-error: " + e.getMessage());
    }
    return custom;
  }

  /** Engine resolution for Gradle {@code hermesDoctor} (sibling modules, Maven local, or HERMES_HOME). */
  public static CheckResult checkEngineResolution(
      boolean siblingHermesApi, boolean hermesHomeCheckout, File hermesHome, String engineVersion) {
    if (siblingHermesApi) {
      return new CheckResult("engine", Status.OK, "Using sibling projects hermes-api / hermes-core.", null);
    }
    File mavenLocalJar = mavenLocalHermesApiJar(engineVersion);
    if (mavenLocalJar.isFile()) {
      return new CheckResult("engine", Status.OK, "Maven local hermes-api " + engineVersion + " found.", null);
    }
    if (hermesHomeCheckout) {
      String homePath = hermesHome != null ? hermesHome.getAbsolutePath() : "?";
      return new CheckResult(
          "engine",
          Status.WARN,
          "HERMES_HOME engine checkout: "
              + homePath
              + ". Launchers can sync but engine JARs need Maven local or sibling modules.",
          "Run ./gradlew publishToMavenLocal from Hermes, or include :hermes-api in settings.");
    }
    return new CheckResult(
        "engine",
        Status.FAIL,
        "No hermes-api in build and not in Maven local (" + mavenLocalJar.getAbsolutePath() + ").",
        "Run ./gradlew publishToMavenLocal from Hermes, set HERMES_HOME, or include engine modules.");
  }

  private static File mavenLocalHermesApiJar(String version) {
    return Path.of(
            System.getProperty("user.home"),
            ".m2/repository/dev/hermes/hermes-api",
            version,
            "hermes-api-" + version + ".jar")
        .toFile();
  }

  private static CheckResult checkMavenLocalArtifacts(Path projectDir) {
    String version = "0.1.0-SNAPSHOT";
    if (projectDir != null) {
      String fromProps =
          dev.hermes.tooling.gradle.GradlePropertySupport.readProperty(
              projectDir, "hermes.engineVersion");
      if (fromProps != null && !fromProps.isBlank()) {
        version = fromProps;
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
    return new CheckResult("maven-local", Status.OK, "Found " + apiJar.getName(), null);
  }
}
