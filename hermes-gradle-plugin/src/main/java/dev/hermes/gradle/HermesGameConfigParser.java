package dev.hermes.gradle;

import dev.hermes.tooling.HermesConfigException;
import java.io.File;
import org.gradle.api.GradleException;

public final class HermesGameConfigParser {

  private HermesGameConfigParser() {}

  public static HermesGameConfig parse(File file) {
    try {
      dev.hermes.tooling.HermesGameConfig parsed = dev.hermes.tooling.HermesGameConfigParser.parse(file);
      HermesGameConfig config = new HermesGameConfig();
      config.setTitle(parsed.getTitle());
      config.setScene(parsed.getScene());
      return config;
    } catch (HermesConfigException e) {
      throw new GradleException(e.getMessage(), e);
    }
  }
}
