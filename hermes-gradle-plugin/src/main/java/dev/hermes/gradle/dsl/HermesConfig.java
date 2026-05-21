package dev.hermes.gradle.dsl;

import dev.hermes.gradle.internal.HermesEngineVersion;
import dev.hermes.tooling.platform.Platforms;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

/** Merged Hermes configuration: settings enable flags + {@code :game} DSL. */
public final class HermesConfig {

  public static final String SETTINGS_PLATFORMS_PROPERTY = "hermesSettingsPlatforms";
  public static final String ENGINE_VERSION_PROPERTY = "hermesEngineVersion";

  private final Project gameProject;
  private final HermesExtension game;
  private final Platforms platforms;

  private HermesConfig(Project gameProject, HermesExtension game, Platforms platforms) {
    this.gameProject = gameProject;
    this.game = game;
    this.platforms = platforms;
  }

  public static HermesConfig resolve(Project gameProject) {
    HermesExtension game = gameProject.getExtensions().getByType(HermesExtension.class);
    SettingsPlatformsExtension settingsPlatforms = resolveSettingsPlatforms(gameProject);
    Platforms merged = Platforms.merge(settingsPlatforms.asPlatforms(), game.getPlatforms().asPlatforms());
    return new HermesConfig(gameProject, game, merged);
  }

  public Project getGameProject() {
    return gameProject;
  }

  public HermesExtension getGame() {
    return game;
  }

  public Platforms getPlatforms() {
    return platforms;
  }

  public static SettingsPlatformsExtension resolveSettingsPlatforms(Project gameProject) {
    var extra = gameProject.getRootProject().getExtensions().getExtraProperties();
    if (!extra.has(SETTINGS_PLATFORMS_PROPERTY)) {
      throw new IllegalStateException(
          "Hermes settings platforms not configured. Apply dev.hermes.settings in settings.gradle.");
    }
    Object value = extra.get(SETTINGS_PLATFORMS_PROPERTY);
    if (value instanceof SettingsPlatformsExtension settingsPlatforms) {
      return settingsPlatforms;
    }
    throw new IllegalStateException(
        "Invalid " + SETTINGS_PLATFORMS_PROPERTY + " type: " + value.getClass().getName());
  }

  public static SettingsPlatformsExtension resolveSettingsPlatforms(Settings settings) {
    HermesSettingsExtension extension = settings.getExtensions().getByType(HermesSettingsExtension.class);
    return extension.getPlatforms();
  }

  public static String resolveEngineVersion(Project gameProject) {
    var extra = gameProject.getRootProject().getExtensions().getExtraProperties();
    if (extra.has(ENGINE_VERSION_PROPERTY)) {
      Object value = extra.get(ENGINE_VERSION_PROPERTY);
      if (value != null && !value.toString().isBlank()) {
        return value.toString();
      }
    }
    if (gameProject.getRootProject().hasProperty(HermesEngineVersion.GRADLE_PROPERTY)) {
      return gameProject.getRootProject().property(HermesEngineVersion.GRADLE_PROPERTY).toString();
    }
    if (gameProject.hasProperty(HermesEngineVersion.GRADLE_PROPERTY)) {
      return gameProject.property(HermesEngineVersion.GRADLE_PROPERTY).toString();
    }
    return gameProject.getVersion().toString();
  }
}
