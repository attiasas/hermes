package dev.hermes.gradle;

import java.io.File;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

/** Settings plugin: conditionally includes launcher modules from the Hermes platforms DSL. */
public final class HermesSettingsPlugin implements Plugin<Settings> {

  @Override
  public void apply(Settings settings) {
    AndroidSdkResolver.resolve(settings);
    HermesSettingsExtension extension = new HermesSettingsExtension();
    settings.getExtensions().add("hermes", extension);
    settings
        .getGradle()
        .settingsEvaluated(
            gradle -> {
              String engineVersion = HermesEngineVersion.resolve(settings, extension);
              File hermesHome = HermesHomeResolver.resolve(settings);
              HermesEnginePropertyPropagator.applyFromHome(settings, hermesHome);
              includeLauncher(settings, extension, "hermes-launcher-desktop", extension.getPlatforms().getDesktop().isEnabled(), engineVersion);
              includeLauncher(settings, extension, "hermes-launcher-html", extension.getPlatforms().getHtml().isEnabled(), engineVersion);
              includeLauncher(
                  settings,
                  extension,
                  "hermes-launcher-android",
                  extension.getPlatforms().getAndroid().isEnabled(),
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
