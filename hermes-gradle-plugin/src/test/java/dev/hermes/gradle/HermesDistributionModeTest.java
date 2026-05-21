package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

final class HermesDistributionModeTest {

  @Test
  void exportDesktopJvmArgsForceDebugFalse() {
    Project root = ProjectBuilder.builder().withName("root").build();
    root.getExtensions()
        .getExtraProperties()
        .set(HermesConfig.SETTINGS_PLATFORMS_PROPERTY, new SettingsPlatformsExtension());

    Project game = ProjectBuilder.builder().withName("game").withParent(root).build();
    HermesExtension extension = game.getExtensions().create("hermes", HermesExtension.class);
    extension.setApplicationClass("dev.hermes.sample.SampleApplication");

    HermesConfig config = HermesConfig.resolve(game);
    HermesGameConfig gameConfig = new HermesGameConfig();
    HermesDistributionMode.ProjectHermesContext context =
        new HermesDistributionMode.ProjectHermesContext(
            game, config, extension.getApplicationClass(), gameConfig);

    JavaExec task = game.getTasks().create("exportDesktopExec", JavaExec.class);
    HermesDistributionMode.applyDesktop(task, context);

    List<String> jvmArgs = task.getJvmArgs();
    assertTrue(jvmArgs.contains("-Dhermes.debug=false"), "export must force debug off");
    assertFalse(
        jvmArgs.stream().anyMatch(arg -> arg.contains("hermes.debug.port")),
        "export must not set HDP port");
  }
}
