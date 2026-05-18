package dev.hermes.tooling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** libGDX / launcher version keys for generated game projects. */
public final class HermesEngineVersions {

  public static final String[] GRADLE_PROPERTY_KEYS = {
    "gdxVersion",
    "lwjgl3Version",
    "gdxTeaVMVersion",
    "android.useAndroidX",
  };

  private static final String DEFAULT_GDX_VERSION = "1.14.0";
  private static final String DEFAULT_LWJGL3_VERSION = "3.4.1";
  private static final String DEFAULT_GDX_TEAVM_VERSION = "1.5.5";
  private static final String DEFAULT_ANDROID_USE_ANDROIDX = "true";

  private HermesEngineVersions() {}

  public static Properties defaults() {
    Properties props = new Properties();
    props.setProperty("gdxVersion", DEFAULT_GDX_VERSION);
    props.setProperty("lwjgl3Version", DEFAULT_LWJGL3_VERSION);
    props.setProperty("gdxTeaVMVersion", DEFAULT_GDX_TEAVM_VERSION);
    props.setProperty("android.useAndroidX", DEFAULT_ANDROID_USE_ANDROIDX);
    return props;
  }

  public static Properties loadFromEngineHome(File hermesHome) {
    if (hermesHome == null || !HermesHomeResolver.isHermesCheckout(hermesHome)) {
      return null;
    }
    Path propsFile = hermesHome.toPath().resolve("gradle.properties");
    if (!Files.isRegularFile(propsFile)) {
      return null;
    }
    Properties props = new Properties();
    try (InputStream in = Files.newInputStream(propsFile)) {
      props.load(in);
      return props;
    } catch (IOException e) {
      return null;
    }
  }

  /** Engine checkout overrides on top of bundled defaults. */
  public static Properties resolveForNewProject(File hermesHome) {
    Properties resolved = defaults();
    Properties fromEngine = loadFromEngineHome(hermesHome);
    if (fromEngine == null) {
      return resolved;
    }
    for (String key : GRADLE_PROPERTY_KEYS) {
      String value = fromEngine.getProperty(key);
      if (value != null && !value.isBlank()) {
        resolved.setProperty(key, value);
      }
    }
    return resolved;
  }
}
