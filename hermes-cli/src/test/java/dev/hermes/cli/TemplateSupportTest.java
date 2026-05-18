package dev.hermes.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplateSupportTest {

  private static final String SPI_PATH =
      "game/src/main/resources/META-INF/services/dev.hermes.api.ecs.ComponentRegistration";

  @Test
  void materialize_gradlewIsExecutable(@TempDir Path target) throws Exception {
    TemplateSupport.materializeEmptyTemplate(target, "MyGame", "dev.hermes.mygame", "0.1.0-SNAPSHOT");

    Path gradlew = target.resolve("gradlew");
    assertTrue(Files.isRegularFile(gradlew), "gradlew missing");
    assertTrue(Files.isExecutable(gradlew), "gradlew must be executable after hermes new");
  }

  @Test
  void materialize_includesGdxVersion(@TempDir Path target) throws Exception {
    TemplateSupport.materializeEmptyTemplate(target, "MyGame", "dev.hermes.mygame", "0.1.0-SNAPSHOT");

    String props = Files.readString(target.resolve("gradle.properties"), StandardCharsets.UTF_8);
    assertTrue(props.contains("gdxVersion="), "gradle.properties must include gdxVersion for synced launchers");
    assertFalse(props.contains("hermes.home="), "game projects must not set hermes.home");
    assertFalse(props.contains("hermes.pluginBuild="), "game projects must not use composite plugin build");
  }

  @Test
  void materialize_substitutesSpiPackage(@TempDir Path target) throws Exception {
    TemplateSupport.materializeEmptyTemplate(target, "MyGame", "dev.hermes.mygame", "0.1.0-SNAPSHOT");

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
    TemplateSupport.materializeEmptyTemplate(target, "MyGame", "dev.hermes.mygame", "0.1.0-SNAPSHOT");

    String settings = Files.readString(target.resolve("settings.gradle"), StandardCharsets.UTF_8);
    assertTrue(settings.contains("mavenLocal()"), "must resolve Hermes plugins from Maven local");
    assertTrue(
        settings.contains("dependencyResolutionManagement"),
        "must declare repositories for synced launcher modules");
    assertFalse(settings.contains("includeBuild"), "must not composite-include hermes-gradle-plugin in IDE");
  }
}
