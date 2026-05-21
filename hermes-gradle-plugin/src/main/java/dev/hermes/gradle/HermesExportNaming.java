package dev.hermes.gradle;

import dev.hermes.tooling.config.HermesGameConfig;
import java.util.Locale;

final class HermesExportNaming {

  private HermesExportNaming() {}

  static String sanitizeTitle(String title) {
    if (title == null || title.isBlank()) {
      return "HermesGame";
    }
    String sanitized = title.replaceAll("[^A-Za-z0-9._-]", "");
    return sanitized.isBlank() ? "HermesGame" : sanitized;
  }

  static String version(org.gradle.api.Project gameProject) {
    Object version = gameProject.getVersion();
    return version == null ? "0.0.0" : version.toString();
  }

  static String zipBaseName(org.gradle.api.Project gameProject, String suffix) {
    HermesGameConfig config = HermesGameConfigs.parse(gameProject);
    return sanitizeTitle(config.getTitle()) + "-" + version(gameProject) + "-" + suffix;
  }

  static String desktopZipSuffix(String target) {
    return switch (target) {
      case "linuxX64" -> "linux-x64";
      case "macM1" -> "macos-aarch64";
      case "macX64" -> "macos-x64";
      case "winX64" -> "windows-x64";
      default -> target.toLowerCase(Locale.ROOT);
    };
  }
}
