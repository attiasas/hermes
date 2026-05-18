package dev.hermes.gradle;

import java.io.File;
import org.gradle.api.Project;

/** Resolves the game assets directory from the Hermes Gradle DSL. */
public final class HermesAssets {

  static final String DEFAULT_ASSETS_DIRECTORY = "src/main/resources/assets";

  private HermesAssets() {}

  public static File resolve(Project gameProject, HermesExtension extension) {
    String configured = extension.getAssetsDirectory();
    String path =
        (configured == null || configured.isBlank()) ? DEFAULT_ASSETS_DIRECTORY : configured;
    return gameProject.file(path);
  }
}
