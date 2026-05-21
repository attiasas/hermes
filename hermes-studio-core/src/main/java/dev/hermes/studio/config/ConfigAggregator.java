package dev.hermes.studio.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.hermes.tooling.HermesGameConfig;
import dev.hermes.tooling.HermesGameConfigParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Loads and saves unified project config across hermes.json and Gradle sources. */
public final class ConfigAggregator {

  private static final Gson GSON = new Gson();
  private static final String GAME_HERMES_JSON = "game/hermes.json";
  private static final String GAME_BUILD_GRADLE = "game/build.gradle";
  private static final String SETTINGS_GRADLE = "settings.gradle";

  public HermesProjectConfigView load(Path root) throws IOException {
    Path projectRoot = root.toAbsolutePath().normalize();
    Path hermesJson = projectRoot.resolve(GAME_HERMES_JSON);
    Path gameBuild = projectRoot.resolve(GAME_BUILD_GRADLE);
    Path settings = projectRoot.resolve(SETTINGS_GRADLE);

    HermesGameConfig gameConfig = HermesGameConfigParser.parse(hermesJson.toFile());

    String gameBuildContent = GradleHermesBlockParser.readFile(gameBuild);
    String hermesBlock =
        GradleHermesBlockParser.findHermesBlock(gameBuildContent).orElse("hermes {\n}\n");

    String applicationClass =
        GradleHermesBlockParser.getStringAssignment(hermesBlock, "applicationClass");
    String assetsDirectory =
        GradleHermesBlockParser.getStringAssignment(hermesBlock, "assetsDirectory");
    Boolean debug = GradleHermesBlockParser.getBooleanAssignment(hermesBlock, "debug");
    String version = GradleHermesBlockParser.getVersionAssignment(gameBuildContent);

    HermesProjectConfigView.GameSection game =
        new HermesProjectConfigView.GameSection(
            gameConfig.getTitle(), gameConfig.getScene(), GAME_HERMES_JSON);

    HermesProjectConfigView.ProjectSection project =
        new HermesProjectConfigView.ProjectSection(
            applicationClass != null ? applicationClass : "",
            assetsDirectory != null ? assetsDirectory : "src/main/resources/assets",
            debug != null && debug,
            version != null ? version : "0.1.0",
            GAME_BUILD_GRADLE);

    String settingsContent = GradleHermesBlockParser.readFile(settings);
    Map<String, Boolean> enabled = GradleHermesBlockParser.parsePlatformEnabled(settingsContent);
    Map<String, int[]> dimensions =
        GradleHermesBlockParser.parsePlatformDimensions(gameBuildContent);

    HermesProjectConfigView.PlatformsSection platforms =
        new HermesProjectConfigView.PlatformsSection(
            platformEntry(enabled, dimensions, "desktop"),
            platformEntry(enabled, dimensions, "html"),
            platformEntry(enabled, dimensions, "android"),
            SETTINGS_GRADLE + ", " + GAME_BUILD_GRADLE);

    return new HermesProjectConfigView(game, project, platforms);
  }

  public void save(Path root, HermesProjectConfigView view) throws IOException {
    Path projectRoot = root.toAbsolutePath().normalize();
    saveHermesJson(projectRoot, view.game());
    saveGameBuildGradle(projectRoot, view.project(), view.platforms());
    saveSettingsGradle(projectRoot, view.platforms());
  }

  /** Merges non-null top-level sections from {@code patch} into {@code current}. */
  public HermesProjectConfigView merge(HermesProjectConfigView current, JsonObject patch) {
    HermesProjectConfigView merged = current;
    if (patch.has("game")) {
      HermesProjectConfigView.GameSection g = current.game();
      JsonObject game = patch.getAsJsonObject("game");
      merged =
          new HermesProjectConfigView(
              new HermesProjectConfigView.GameSection(
                  stringOr(game, "title", g.title()),
                  stringOr(game, "scene", g.scene()),
                  g.sourceFile()),
              merged.project(),
              merged.platforms());
    }
    if (patch.has("project")) {
      HermesProjectConfigView.ProjectSection p = merged.project();
      JsonObject project = patch.getAsJsonObject("project");
      merged =
          new HermesProjectConfigView(
              merged.game(),
              new HermesProjectConfigView.ProjectSection(
                  stringOr(project, "applicationClass", p.applicationClass()),
                  stringOr(project, "assetsDirectory", p.assetsDirectory()),
                  project.has("debug") ? project.get("debug").getAsBoolean() : p.debug(),
                  stringOr(project, "version", p.version()),
                  p.sourceFile()),
              merged.platforms());
    }
    if (patch.has("platforms")) {
      JsonObject platforms = patch.getAsJsonObject("platforms");
      merged =
          new HermesProjectConfigView(
              merged.game(),
              merged.project(),
              mergePlatforms(merged.platforms(), platforms));
    }
    return merged;
  }

  private static HermesProjectConfigView.PlatformsSection mergePlatforms(
      HermesProjectConfigView.PlatformsSection current, JsonObject patch) {
    return new HermesProjectConfigView.PlatformsSection(
        mergePlatform(current.desktop(), patch, "desktop"),
        mergePlatform(current.html(), patch, "html"),
        mergePlatform(current.android(), patch, "android"),
        current.sourceFile());
  }

  private static HermesProjectConfigView.PlatformEntry mergePlatform(
      HermesProjectConfigView.PlatformEntry current, JsonObject patch, String name) {
    if (!patch.has(name)) {
      return current;
    }
    JsonObject platform = patch.getAsJsonObject(name);
    return new HermesProjectConfigView.PlatformEntry(
        platform.has("enabled") ? platform.get("enabled").getAsBoolean() : current.enabled(),
        platform.has("width") ? platform.get("width").getAsInt() : current.width(),
        platform.has("height") ? platform.get("height").getAsInt() : current.height());
  }

  private static String stringOr(JsonObject object, String key, String fallback) {
    return object.has(key) ? object.get(key).getAsString() : fallback;
  }

  private static HermesProjectConfigView.PlatformEntry platformEntry(
      Map<String, Boolean> enabled, Map<String, int[]> dimensions, String platform) {
    boolean isEnabled = enabled.getOrDefault(platform, true);
    int[] size = dimensions.get(platform);
    Integer width = size != null ? size[0] : null;
    Integer height = size != null ? size[1] : null;
    return new HermesProjectConfigView.PlatformEntry(isEnabled, width, height);
  }

  private static void saveHermesJson(Path root, HermesProjectConfigView.GameSection game)
      throws IOException {
    JsonObject json = new JsonObject();
    json.addProperty("title", game.title());
    json.addProperty("scene", game.scene());
    Path path = root.resolve(GAME_HERMES_JSON);
    Files.writeString(path, GSON.toJson(json) + "\n");
  }

  private static void saveGameBuildGradle(
      Path root, HermesProjectConfigView.ProjectSection project, HermesProjectConfigView.PlatformsSection platforms)
      throws IOException {
    Path gameBuild = root.resolve(GAME_BUILD_GRADLE);
    String content = GradleHermesBlockParser.readFile(gameBuild);
    String block =
        GradleHermesBlockParser.findHermesBlock(content)
            .orElseThrow(() -> new IOException("Missing hermes block in " + GAME_BUILD_GRADLE));

    block = GradleHermesBlockParser.setStringAssignment(block, "applicationClass", project.applicationClass());
    block = GradleHermesBlockParser.setStringAssignment(block, "assetsDirectory", project.assetsDirectory());
    block = GradleHermesBlockParser.setBooleanAssignment(block, "debug", project.debug());

    if (platforms.desktop().width() != null && platforms.desktop().height() != null) {
      block =
          GradleHermesBlockParser.setIntAssignmentInPlatform(
              block, "desktop", "width", platforms.desktop().width());
      block =
          GradleHermesBlockParser.setIntAssignmentInPlatform(
              block, "desktop", "height", platforms.desktop().height());
    }
    if (platforms.html().width() != null && platforms.html().height() != null) {
      block =
          GradleHermesBlockParser.setIntAssignmentInPlatform(
              block, "html", "width", platforms.html().width());
      block =
          GradleHermesBlockParser.setIntAssignmentInPlatform(
              block, "html", "height", platforms.html().height());
    }

    content = GradleHermesBlockParser.replaceHermesBlock(content, block);
    content = GradleHermesBlockParser.setVersionAssignment(content, project.version());
    GradleHermesBlockParser.writeFile(gameBuild, content);
  }

  private static void saveSettingsGradle(Path root, HermesProjectConfigView.PlatformsSection platforms)
      throws IOException {
    Path settings = root.resolve(SETTINGS_GRADLE);
    String content = GradleHermesBlockParser.readFile(settings);
    content = GradleHermesBlockParser.setPlatformEnabled(content, "desktop", platforms.desktop().enabled());
    content = GradleHermesBlockParser.setPlatformEnabled(content, "html", platforms.html().enabled());
    content = GradleHermesBlockParser.setPlatformEnabled(content, "android", platforms.android().enabled());
    GradleHermesBlockParser.writeFile(settings, content);
  }
}
