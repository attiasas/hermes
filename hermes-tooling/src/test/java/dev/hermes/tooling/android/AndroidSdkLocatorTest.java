package dev.hermes.tooling.android;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AndroidSdkLocatorTest {

  @Test
  void locate_prefersLocalPropertiesOverGradleProperty(@TempDir Path projectDir) throws Exception {
    Path sdkRoot = projectDir.resolve("fake-sdk");
    Files.createDirectories(sdkRoot.resolve("platform-tools"));
    Files.writeString(
        projectDir.resolve("local.properties"),
        "sdk.dir=" + sdkRoot.toAbsolutePath() + "\n",
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
        "hermes.android.sdk=" + sdkRoot.toAbsolutePath() + "\n",
        StandardCharsets.UTF_8);

    assertEquals(sdkRoot.toFile().getAbsoluteFile(), AndroidSdkLocator.locate(projectDir));
  }

  @Test
  void locate_returnsNullWhenNothingValid(@TempDir Path projectDir) {
    assertNull(AndroidSdkLocator.locate(projectDir));
  }
}
