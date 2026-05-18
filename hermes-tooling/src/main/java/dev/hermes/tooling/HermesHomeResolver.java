package dev.hermes.tooling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class HermesHomeResolver {

  public static final String ENV_HERMES_HOME = "HERMES_HOME";
  public static final String GRADLE_PROPERTY = "hermes.home";

  private HermesHomeResolver() {}

  public static File resolve(Path projectDir) {
    String fromProps = readGradleProperties(projectDir, GRADLE_PROPERTY);
    String fromEnv = System.getenv(ENV_HERMES_HOME);
    String path = firstNonBlank(fromProps, fromEnv);
    if (path == null || path.isBlank()) {
      return null;
    }
    File home = new File(path);
    return home.isDirectory() ? home.getAbsoluteFile() : null;
  }

  public static boolean isHermesCheckout(File home) {
    return home != null
        && home.isDirectory()
        && new File(home, "hermes-api").isDirectory()
        && new File(home, "hermes-core").isDirectory();
  }

  private static String readGradleProperties(Path projectDir, String key) {
    Path props = projectDir.resolve("gradle.properties");
    if (!Files.isRegularFile(props)) {
      return null;
    }
    Properties properties = new Properties();
    try (var in = Files.newInputStream(props)) {
      properties.load(in);
      return properties.getProperty(key);
    } catch (IOException e) {
      return null;
    }
  }

  private static String firstNonBlank(String first, String second) {
    if (first != null && !first.isBlank()) {
      return first;
    }
    if (second != null && !second.isBlank()) {
      return second;
    }
    return null;
  }
}
