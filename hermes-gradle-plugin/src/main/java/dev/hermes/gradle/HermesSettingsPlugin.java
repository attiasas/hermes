package dev.hermes.gradle;

import java.io.File;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

/** Settings plugin: conditionally includes launcher modules from the Hermes platforms DSL. */
public final class HermesSettingsPlugin implements Plugin<Settings> {

  @Override
  public void apply(Settings settings) {
    AndroidSdkResolver.resolve(settings);
    // Maven-first game projects have no root buildscript; AGP must be in pluginManagement.
    HermesAndroidGradlePlugin.register(settings);
    HermesSettingsExtension extension =
        settings.getExtensions().create("hermes", HermesSettingsExtension.class);
    settings
        .getGradle()
        .settingsEvaluated(
            gradle -> {
              String engineVersion = HermesEngineVersion.resolve(settings, extension);
              File hermesHome = HermesHomeResolver.resolve(settings);
              HermesEnginePropertyPropagator.apply(settings, hermesHome);
              SettingsPlatformsExtension platforms = extension.getPlatforms();
              settings
                  .getGradle()
                  .beforeProject(
                      project -> {
                        project
                            .getExtensions()
                            .getExtraProperties()
                            .set(HermesConfig.SETTINGS_PLATFORMS_PROPERTY, platforms);
                        project
                            .getExtensions()
                            .getExtraProperties()
                            .set(HermesConfig.ENGINE_VERSION_PROPERTY, engineVersion);
                      });
              includeLauncher(settings, extension, "hermes-launcher-desktop", platforms.getDesktop().isEnabled(), engineVersion);
              includeLauncher(settings, extension, "hermes-launcher-html", platforms.getHtml().isEnabled(), engineVersion);
              includeLauncher(
                  settings,
                  extension,
                  "hermes-launcher-android",
                  platforms.getAndroid().isEnabled(),
                  engineVersion);
            });
  }

  private static void includeLauncher(
      Settings settings,
      HermesSettingsExtension extension,
      String moduleName,
      boolean enabled,
      String engineVersion) {
    if (!enabled) {
      return;
    }
    if (settings.findProject(":" + moduleName) != null) {
      return;
    }
    if (new File(settings.getRootDir(), moduleName).isDirectory()) {
      settings.include(moduleName);
      return;
    }
    HermesPlatformSync.syncIfNeeded(settings, moduleName, engineVersion);
    File synced = HermesPlatformSync.launcherDir(settings, moduleName);
    if (!synced.isDirectory()) {
      return;
    }
    settings.include(moduleName);
    settings.project(":" + moduleName).setProjectDir(synced);
  }
}
