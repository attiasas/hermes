package dev.hermes.gradle;

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
              if (extension.getPlatforms().getDesktop().isEnabled()) {
                settings.include("hermes-launcher-desktop");
              }
              if (extension.getPlatforms().getHtml().isEnabled()) {
                settings.include("hermes-launcher-html");
              }
              if (extension.getPlatforms().getAndroid().isEnabled()) {
                settings.include("hermes-launcher-android");
              }
            });
  }
}
