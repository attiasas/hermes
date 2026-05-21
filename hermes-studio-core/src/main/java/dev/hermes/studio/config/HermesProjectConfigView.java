package dev.hermes.studio.config;

/** Unified Hermes project settings across hermes.json and Gradle files. */
public record HermesProjectConfigView(
    GameSection game, ProjectSection project, PlatformsSection platforms) {

  public record GameSection(String title, String scene, String sourceFile) {}

  public record ProjectSection(
      String applicationClass,
      String assetsDirectory,
      boolean debug,
      String version,
      String sourceFile) {}

  public record PlatformsSection(
      PlatformEntry desktop, PlatformEntry html, PlatformEntry android, String sourceFile) {}

  /** {@code enabled} from settings.gradle; {@code width}/{@code height} from game/build.gradle. */
  public record PlatformEntry(boolean enabled, Integer width, Integer height) {}
}
