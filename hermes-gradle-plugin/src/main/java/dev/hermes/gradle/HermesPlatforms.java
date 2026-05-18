package dev.hermes.gradle;

import org.gradle.api.Project;

/** Resolves the settings-scoped {@link PlatformsExtension} from a game project. */
public final class HermesPlatforms {

  static final String PROJECT_EXTRA_PROPERTY = "hermesPlatforms";

  private HermesPlatforms() {}

  public static PlatformsExtension resolve(Project gameProject) {
    var extra = gameProject.getExtensions().getExtraProperties();
    if (!extra.has(PROJECT_EXTRA_PROPERTY)) {
      throw new IllegalStateException(
          "Hermes platforms not configured. Apply the dev.hermes.settings plugin in settings.gradle.");
    }
    Object value = extra.get(PROJECT_EXTRA_PROPERTY);
    if (value instanceof PlatformsExtension platforms) {
      return platforms;
    }
    throw new IllegalStateException("Invalid hermesPlatforms extra property type: " + value.getClass().getName());
  }
}
