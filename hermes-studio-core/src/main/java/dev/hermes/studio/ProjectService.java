package dev.hermes.studio;

import java.nio.file.Files;
import java.nio.file.Path;

/** Locates and validates a Hermes Gradle project on disk. */
public final class ProjectService {

  private static final String GAME_DIR = "game";
  private static final String HERMES_JSON = "hermes.json";
  private static final String SETTINGS_GRADLE = "settings.gradle";
  private static final String DEFAULT_ASSETS = "src/main/resources/assets";

  public HermesProject open(Path root) {
    Path projectRoot = root.toAbsolutePath().normalize();
    if (!Files.isDirectory(projectRoot)) {
      throw new IllegalArgumentException("Not a directory: " + root);
    }
    Path settingsGradle = projectRoot.resolve(SETTINGS_GRADLE);
    if (!Files.isRegularFile(settingsGradle)) {
      throw new IllegalArgumentException("Missing " + SETTINGS_GRADLE + " in " + projectRoot);
    }
    Path gameDir = projectRoot.resolve(GAME_DIR);
    Path hermesJson = gameDir.resolve(HERMES_JSON);
    if (!Files.isRegularFile(hermesJson)) {
      throw new IllegalArgumentException("Missing " + GAME_DIR + "/" + HERMES_JSON + " in " + projectRoot);
    }
    Path assetsDir = gameDir.resolve(DEFAULT_ASSETS);
    return new HermesProject(projectRoot, gameDir, hermesJson, assetsDir);
  }
}
