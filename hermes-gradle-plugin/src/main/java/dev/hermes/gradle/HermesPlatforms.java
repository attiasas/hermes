package dev.hermes.gradle;

import dev.hermes.gradle.dsl.HermesConfig;
import dev.hermes.tooling.platform.Platforms;
import org.gradle.api.Project;

/** Resolves merged platform configuration for a game project. */
public final class HermesPlatforms {

  private HermesPlatforms() {}

  public static Platforms resolve(Project gameProject) {
    return HermesConfig.resolve(gameProject).getPlatforms();
  }
}
