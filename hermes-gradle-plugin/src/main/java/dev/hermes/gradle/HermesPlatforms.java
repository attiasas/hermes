package dev.hermes.gradle;

import org.gradle.api.Project;

/** Resolves merged platform configuration for a game project. */
public final class HermesPlatforms {

  /** @deprecated use {@link HermesConfig#SETTINGS_PLATFORMS_PROPERTY} */
  @Deprecated
  static final String PROJECT_EXTRA_PROPERTY = HermesConfig.SETTINGS_PLATFORMS_PROPERTY;

  private HermesPlatforms() {}

  public static PlatformsExtension resolve(Project gameProject) {
    return HermesConfig.resolve(gameProject).getPlatforms();
  }
}
