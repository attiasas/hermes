package dev.hermes.gradle;

import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

/** Merged Hermes configuration: settings enable flags + {@code :game} DSL. */
public final class HermesConfig {

  static final String SETTINGS_PLATFORMS_PROPERTY = "hermesSettingsPlatforms";
  static final String ENGINE_VERSION_PROPERTY = "hermesEngineVersion";

  private final Project gameProject;
  private final HermesExtension game;
  private final PlatformsExtension platforms;

  private HermesConfig(Project gameProject, HermesExtension game, PlatformsExtension platforms) {
    this.gameProject = gameProject;
    this.game = game;
    this.platforms = platforms;
  }

  public static HermesConfig resolve(Project gameProject) {
    HermesExtension game = gameProject.getExtensions().getByType(HermesExtension.class);
    SettingsPlatformsExtension settingsPlatforms = resolveSettingsPlatforms(gameProject);
    PlatformsExtension merged = mergePlatforms(settingsPlatforms, game.getPlatforms());
    return new HermesConfig(gameProject, game, merged);
  }

  public Project getGameProject() {
    return gameProject;
  }

  public HermesExtension getGame() {
    return game;
  }

  public PlatformsExtension getPlatforms() {
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

  private static PlatformsExtension mergePlatforms(
      SettingsPlatformsExtension settings, GamePlatformsExtension game) {
    PlatformsExtension merged = new PlatformsExtension(false);
    mergeDesktop(merged.getDesktop(), settings.getDesktop(), game.getDesktop());
    mergeHtml(merged.getHtml(), settings.getHtml(), game.getHtml());
    mergeAndroid(merged.getAndroid(), settings.getAndroid(), game.getAndroid());
    return merged;
  }

  private static void mergeDesktop(
      DesktopPlatformSpec target, PlatformEnableSpec enabled, GameDesktopPlatformSpec game) {
    target.setEnabled(enabled.isEnabled());
    target.setWidth(game.getWidth());
    target.setHeight(game.getHeight());
    target.setVsync(game.isVsync());
    target.setResizable(game.isResizable());
    target.setForegroundFps(game.getForegroundFps());
    target.setBundleId(game.getBundleId());
    target.setExecutableName(game.getExecutableName());
    target.setExportTargets(game.getExportTargets());
  }

  private static void mergeHtml(
      HtmlPlatformSpec target, PlatformEnableSpec enabled, GameHtmlPlatformSpec game) {
    target.setEnabled(enabled.isEnabled());
    target.setWidth(game.getWidth());
    target.setHeight(game.getHeight());
    target.setDevServerPort(game.getDevServerPort());
    target.setWebAssembly(game.isWebAssembly());
  }

  private static void mergeAndroid(
      AndroidPlatformSpec target, PlatformEnableSpec enabled, GameAndroidPlatformSpec game) {
    target.setEnabled(enabled.isEnabled());
    target.setApplicationId(game.getApplicationId());
    target.setMinSdk(game.getMinSdk());
    target.setTargetSdk(game.getTargetSdk());
    target.setCompileSdk(game.getCompileSdk());
    target.setVersionCode(game.getVersionCode());
    target.setScreenOrientation(game.getScreenOrientation());
  }
}
