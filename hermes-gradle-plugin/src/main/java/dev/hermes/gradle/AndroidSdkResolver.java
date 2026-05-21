package dev.hermes.gradle;

import dev.hermes.tooling.android.AndroidSdkLocator;
import dev.hermes.tooling.android.AndroidSdkValidator;
import dev.hermes.tooling.gradle.GradlePropertySupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logging;

/**
 * Resolves the Android SDK for AGP from {@link AndroidSdkLocator} and writes {@code local.properties}
 * when a path is found via Gradle property or environment but {@code sdk.dir} is unset.
 */
public final class AndroidSdkResolver {

  public static final String GRADLE_PROPERTY = AndroidSdkLocator.GRADLE_PROPERTY;
  public static final String LOCAL_PROPERTY_KEY = AndroidSdkLocator.LOCAL_PROPERTY_KEY;

  private AndroidSdkResolver() {}

  public static File resolve(Settings settings) {
    File rootDir = settings.getRootDir();
    File localPropertiesFile = new File(rootDir, "local.properties");
    String fromLocal = readSdkDir(localPropertiesFile);
    if (fromLocal != null && !fromLocal.isBlank()) {
      File sdk = new File(fromLocal);
      return AndroidSdkValidator.isValidSdk(sdk) ? sdk : null;
    }

    String fromGradle = readGradleProperty(settings, GRADLE_PROPERTY);
    String fromEnv =
        GradlePropertySupport.firstNonBlank(
            System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"));
    String sdkPath = GradlePropertySupport.firstNonBlank(fromGradle, fromEnv);
    if (sdkPath == null || sdkPath.isBlank()) {
      return null;
    }

    File sdk = new File(sdkPath);
    if (!AndroidSdkValidator.isValidSdk(sdk)) {
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
}
