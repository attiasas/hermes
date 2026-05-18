package dev.hermes.gradle;

import org.gradle.api.Project;

/** Resolves merged platform configuration for a game project. */
public final class HermesPlatforms {

  private HermesPlatforms() {}

  public static PlatformsExtension resolve(Project gameProject) {
    return HermesConfig.resolve(gameProject).getPlatforms();
  }
}
