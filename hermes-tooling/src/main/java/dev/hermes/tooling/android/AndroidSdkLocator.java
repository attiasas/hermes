package dev.hermes.tooling.android;

import dev.hermes.tooling.gradle.GradlePropertySupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Locates the Android SDK from, in order:
 *
 * <ol>
 *   <li>{@code local.properties} {@code sdk.dir}
 *   <li>{@code gradle.properties} {@code hermes.android.sdk}
 *   <li>{@code ANDROID_SDK_ROOT} / {@code ANDROID_HOME}
 * </ol>
 */
public final class AndroidSdkLocator {

  public static final String GRADLE_PROPERTY = "hermes.android.sdk";
  public static final String LOCAL_PROPERTY_KEY = "sdk.dir";

  private AndroidSdkLocator() {}

  public static File locate(Path projectRoot) {
    if (projectRoot == null) {
      return locateFromEnvironmentOnly();
    }
    File fromLocal = readSdkDirFromLocalProperties(projectRoot.resolve("local.properties"));
    if (AndroidSdkValidator.isValidSdk(fromLocal)) {
      return fromLocal;
    }
    String fromGradle = GradlePropertySupport.readProperty(projectRoot, GRADLE_PROPERTY);
    String fromEnv =
        GradlePropertySupport.firstNonBlank(
            System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"));
    String sdkPath = GradlePropertySupport.firstNonBlank(fromGradle, fromEnv);
    if (sdkPath == null || sdkPath.isBlank()) {
      return null;
    }
    File sdk = new File(sdkPath);
    return AndroidSdkValidator.isValidSdk(sdk) ? sdk.getAbsoluteFile() : null;
  }

  public static File locate(File projectRoot) {
    return projectRoot == null ? locateFromEnvironmentOnly() : locate(projectRoot.toPath());
  }

  private static File locateFromEnvironmentOnly() {
    String fromEnv =
        GradlePropertySupport.firstNonBlank(
            System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"));
    if (fromEnv == null || fromEnv.isBlank()) {
      return null;
    }
    File sdk = new File(fromEnv);
    return AndroidSdkValidator.isValidSdk(sdk) ? sdk.getAbsoluteFile() : null;
  }

  private static File readSdkDirFromLocalProperties(Path localProperties) {
    if (!Files.isRegularFile(localProperties)) {
      return null;
    }
    Properties properties = new Properties();
    try (InputStream in = Files.newInputStream(localProperties)) {
      properties.load(in);
    } catch (IOException e) {
      return null;
    }
    String path = properties.getProperty(LOCAL_PROPERTY_KEY);
    if (path == null || path.isBlank()) {
      return null;
    }
    File sdk = new File(path);
    return AndroidSdkValidator.isValidSdk(sdk) ? sdk.getAbsoluteFile() : null;
  }
}
