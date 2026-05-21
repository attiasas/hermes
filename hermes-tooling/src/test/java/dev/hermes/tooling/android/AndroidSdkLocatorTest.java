package dev.hermes.tooling.android;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.hermes.tooling.gradle.GradlePropertySupport;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AndroidSdkLocatorTest {

  /** Properties files treat backslash as escape; use forward slashes on all OSes. */
  private static String propertyPath(Path path) {
    return path.toAbsolutePath().normalize().toString().replace('\\', '/');
  }

  @Test
  void locate_prefersLocalPropertiesOverGradleProperty(@TempDir Path projectDir) throws Exception {
    Path sdkRoot = projectDir.resolve("fake-sdk");
    Files.createDirectories(sdkRoot.resolve("platform-tools"));
    Files.writeString(
        projectDir.resolve("local.properties"),
        "sdk.dir=" + propertyPath(sdkRoot) + "\n",
        StandardCharsets.UTF_8);
    Files.writeString(
        projectDir.resolve("gradle.properties"),
        "hermes.android.sdk=/other/sdk\n",
        StandardCharsets.UTF_8);

    assertEquals(sdkRoot.toFile().getAbsoluteFile(), AndroidSdkLocator.locate(projectDir));
  }

  @Test
  void locate_readsGradlePropertyWhenLocalMissing(@TempDir Path projectDir) throws Exception {
    Path sdkRoot = projectDir.resolve("sdk");
    Files.createDirectories(sdkRoot.resolve("platform-tools"));
    Files.writeString(
        projectDir.resolve("gradle.properties"),
        "hermes.android.sdk=" + propertyPath(sdkRoot) + "\n",
        StandardCharsets.UTF_8);

    assertEquals(sdkRoot.toFile().getAbsoluteFile(), AndroidSdkLocator.locate(projectDir));
  }

  @Test
  void locate_withoutProjectConfig_usesEnvironmentOrNull(@TempDir Path projectDir) {
    File located = AndroidSdkLocator.locate(projectDir);
    String fromEnv =
        GradlePropertySupport.firstNonBlank(
            System.getenv("ANDROID_SDK_ROOT"), System.getenv("ANDROID_HOME"));
    if (fromEnv != null && !fromEnv.isBlank()) {
      File envSdk = new File(fromEnv);
      if (AndroidSdkValidator.isValidSdk(envSdk)) {
        assertEquals(envSdk.getAbsoluteFile(), located);
        return;
      }
    }
    assertNull(located);
  }
}
