package dev.hermes.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logging;

/**
 * Resolves the Android SDK for AGP from, in order:
 *
 * <ol>
 *   <li>{@code local.properties} {@code sdk.dir}
 *   <li>Gradle property {@code hermes.android.sdk}
 *   <li>{@code ANDROID_SDK_ROOT}
 *   <li>{@code ANDROID_HOME}
 * </ol>
 *
 * When a path is found via Gradle property or environment and {@code local.properties} does not
 * define {@code sdk.dir}, writes {@code local.properties} so AGP can locate the SDK.
 */
public final class AndroidSdkResolver {

  public static final String GRADLE_PROPERTY = "hermes.android.sdk";
  public static final String LOCAL_PROPERTY_KEY = "sdk.dir";

  private AndroidSdkResolver() {}

  public static File resolve(Settings settings) {
    File rootDir = settings.getRootDir();
    File localPropertiesFile = new File(rootDir, "local.properties");
    String fromLocal = readSdkDir(localPropertiesFile);
    if (fromLocal != null && !fromLocal.isBlank()) {
      return new File(fromLocal);
    }

    String fromGradle = readGradleProperty(settings, GRADLE_PROPERTY);
    String fromEnv = firstNonBlank(System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"));
    String sdkPath = firstNonBlank(fromGradle, fromEnv);
    if (sdkPath == null || sdkPath.isBlank()) {
      return null;
    }

    writeLocalProperties(localPropertiesFile, sdkPath);
    Logging.getLogger(AndroidSdkResolver.class)
        .lifecycle(
        "Hermes: wrote {} from {} (set {} in gradle.properties or export ANDROID_SDK_ROOT to override)",
        localPropertiesFile.getName(),
        sourceLabel(fromGradle, fromEnv),
        GRADLE_PROPERTY);
    return new File(sdkPath);
  }

  private static String readSdkDir(File localPropertiesFile) {
    if (!localPropertiesFile.isFile()) {
      return null;
    }
    Properties properties = new Properties();
    try (FileInputStream in = new FileInputStream(localPropertiesFile)) {
      properties.load(in);
    } catch (IOException e) {
      return null;
    }
    return properties.getProperty(LOCAL_PROPERTY_KEY);
  }

  private static String readGradleProperty(Settings settings, String name) {
    try {
      return settings.getProviders().gradleProperty(name).getOrNull();
    } catch (Exception ignored) {
      return null;
    }
  }

  private static void writeLocalProperties(File localPropertiesFile, String sdkPath) {
    Properties properties = new Properties();
    if (localPropertiesFile.isFile()) {
      try (FileInputStream in = new FileInputStream(localPropertiesFile)) {
        properties.load(in);
      } catch (IOException ignored) {
        // overwrite below
      }
    }
    properties.setProperty(LOCAL_PROPERTY_KEY, sdkPath.replace('\\', '/'));
    try (FileOutputStream out = new FileOutputStream(localPropertiesFile)) {
      properties.store(out, "Android SDK location — see README.md");
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write " + localPropertiesFile, e);
    }
  }

  private static String sourceLabel(String fromGradle, String fromEnv) {
    if (fromGradle != null && !fromGradle.isBlank()) {
      return GRADLE_PROPERTY;
    }
    if (fromEnv != null && !fromEnv.isBlank()) {
      return System.getenv("ANDROID_SDK_ROOT") != null ? "ANDROID_SDK_ROOT" : "ANDROID_HOME";
    }
    return "unknown";
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
