package dev.hermes.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplateSupportTest {

  private static final String SPI_PATH =
      "game/src/main/resources/META-INF/services/dev.hermes.api.ecs.ComponentRegistration";

  private static void materialize(Path target) throws Exception {
    TemplateSupport.materializeEmptyTemplate(
        target, "MyGame", "dev.hermes.mygame", "0.1.0-SNAPSHOT", Set.of("desktop"), null);
  }

  @Test
  void materialize_gradlewIsExecutable(@TempDir Path target) throws Exception {
    materialize(target);

    Path gradlew = target.resolve("gradlew");
    assertTrue(Files.isRegularFile(gradlew), "gradlew missing");
    assertTrue(Files.isExecutable(gradlew), "gradlew must be executable after hermes new");
  }

  @Test
  void materialize_doesNotPinGdxVersionsInGradleProperties(@TempDir Path target) throws Exception {
    materialize(target);

    String props = Files.readString(target.resolve("gradle.properties"), StandardCharsets.UTF_8);
    assertFalse(
        props.contains("gdxVersion="),
        "libGDX versions are injected by dev.hermes.settings, not gradle.properties");
    assertFalse(props.contains("hermes.home="), "game projects must not set hermes.home");
    assertFalse(props.contains("hermes.pluginBuild="), "game projects must not use composite plugin build");
  }

  @Test
  void materialize_substitutesSpiPackage(@TempDir Path target) throws Exception {
    materialize(target);

    Path spi = target.resolve(SPI_PATH);
    assertTrue(Files.isRegularFile(spi), "SPI file missing: " + spi);
    String content = Files.readString(spi, StandardCharsets.UTF_8);
    assertTrue(
        content.contains("dev.hermes.mygame.PulseMarkerRegistration"),
        "Expected substituted registration, got: " + content);
    assertFalse(content.contains("{{package}}"), "Unsubstituted token in SPI: " + content);
  }

  @Test
  void materialize_settingsGradleUsesMavenLocal(@TempDir Path target) throws Exception {
    materialize(target);

    String settings = Files.readString(target.resolve("settings.gradle"), StandardCharsets.UTF_8);
    assertTrue(settings.contains("mavenLocal()"), "must resolve Hermes plugins from Maven local");
    assertTrue(
        settings.contains("dependencyResolutionManagement"),
        "must declare repositories for synced launcher modules");
    assertFalse(settings.contains("includeBuild"), "must not composite-include hermes-gradle-plugin in IDE");
    assertTrue(settings.contains("enabled = true"), "desktop should be enabled by default");
    String gameBuild = Files.readString(target.resolve("game/build.gradle"), StandardCharsets.UTF_8);
    assertTrue(gameBuild.contains("devServerPort"), "game/build.gradle should configure html options");
    assertTrue(gameBuild.contains("screenOrientation"), "game/build.gradle should configure android options");
    assertTrue(gameBuild.contains("platforms {"), "game/build.gradle should own platform details");
    assertTrue(gameBuild.contains("assetsDirectory"), "game/build.gradle should set assetsDirectory");
    assertTrue(gameBuild.contains("icons {"), "game/build.gradle should declare icons DSL");
  }

  @Test
  void materialize_androidSdkOnlyInLocalProperties(@TempDir Path target) throws Exception {
    String sdk = System.getenv("ANDROID_SDK_ROOT");
    if (sdk == null || sdk.isBlank()) {
      sdk = System.getenv("ANDROID_HOME");
    }
    org.junit.jupiter.api.Assumptions.assumeTrue(
        sdk != null && !sdk.isBlank(),
        "ANDROID_SDK_ROOT or ANDROID_HOME required for this test");

    TemplateSupport.materializeEmptyTemplate(
        target, "MyGame", "dev.hermes.mygame", "0.1.0-SNAPSHOT", Set.of("android"), Path.of(sdk));

    String props = Files.readString(target.resolve("gradle.properties"), StandardCharsets.UTF_8);
    assertFalse(props.contains("hermes.android.sdk="), "SDK path belongs in local.properties only");
    String local = Files.readString(target.resolve("local.properties"), StandardCharsets.UTF_8);
    assertTrue(local.contains("sdk.dir="), "local.properties must set sdk.dir");
  }
}
