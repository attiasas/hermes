package dev.hermes.gradle;

import dev.hermes.gradle.dsl.HermesConfig;
import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.platform.DesktopPlatform;
import dev.hermes.tooling.platform.HtmlPlatform;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.tasks.JavaExec;

/** Forces distribution (non-debug) flags on export-related tasks without mutating {@link HermesExtension}. */
final class HermesDistributionMode {

  static final String EXPORT_ACTIVE_PROPERTY = "hermesDistributionExportActive";

  private HermesDistributionMode() {}

  static void registerExportGraphListener(org.gradle.api.Project gameProject) {
    gameProject
        .getGradle()
        .getTaskGraph()
        .whenReady(
            graph -> {
              if (isExportTaskGraph(gameProject, graph)) {
                gameProject.getExtensions().getExtraProperties().set(EXPORT_ACTIVE_PROPERTY, true);
              }
            });
  }

  static boolean isDistributionExport(org.gradle.api.Project gameProject) {
    var extra = gameProject.getExtensions().getExtraProperties();
    return extra.has(EXPORT_ACTIVE_PROPERTY) && Boolean.TRUE.equals(extra.get(EXPORT_ACTIVE_PROPERTY));
  }

  private static boolean isExportTaskGraph(
      org.gradle.api.Project gameProject, org.gradle.api.execution.TaskExecutionGraph graph) {
    return graph.getAllTasks().stream()
        .anyMatch(
            task ->
                task.getProject().equals(gameProject)
                    && (task.getName().startsWith("hermesExport")
                        || task.getName().startsWith("zipHermesExport")
                        || task.getName().startsWith("stageHermesExport")));
  }

  static void applyDesktop(JavaExec task, ProjectHermesContext context) {
    List<String> jvmArgs = new ArrayList<>(task.getJvmArgs());
    HermesJvmArgs.stripNativeAccess(jvmArgs);
    jvmArgs.add("-Dhermes.applicationClass=" + context.applicationClass());
    jvmArgs.add("-Dhermes.debug=false");
    DesktopPlatform desktop = context.config().getPlatforms().getDesktop();
    HermesGameConfig gameConfig = context.gameConfig();
    jvmArgs.add("-Dhermes.window.width=" + desktop.getWidth());
    jvmArgs.add("-Dhermes.window.height=" + desktop.getHeight());
    jvmArgs.add("-Dhermes.window.title=" + gameConfig.getTitle());
    jvmArgs.add("-Dhermes.desktop.vsync=" + desktop.isVsync());
    jvmArgs.add("-Dhermes.desktop.resizable=" + desktop.isResizable());
    jvmArgs.add("-Dhermes.desktop.foregroundFps=" + desktop.getForegroundFps());
    jvmArgs.add("-Dhermes.game.title=" + gameConfig.getTitle());
    jvmArgs.add("-Dhermes.game.scene=" + gameConfig.getScene());
    task.setJvmArgs(jvmArgs);
  }

  static void applyHtml(JavaExec task, ProjectHermesContext context, java.io.File assetsDir) {
    HtmlPlatform html = context.config().getPlatforms().getHtml();
    HermesGameConfig gameConfig = context.gameConfig();
    task.systemProperty("hermes.applicationClass", context.applicationClass());
    task.systemProperty("hermes.debug", "false");
    task.systemProperty("hermes.window.width", String.valueOf(html.getWidth()));
    task.systemProperty("hermes.window.height", String.valueOf(html.getHeight()));
    task.systemProperty("hermes.window.title", gameConfig.getTitle());
    task.systemProperty("hermes.html.devServerPort", String.valueOf(html.getDevServerPort()));
    task.systemProperty("hermes.html.webAssembly", String.valueOf(html.isWebAssembly()));
    task.systemProperty("hermes.assets.dir", assetsDir.getAbsolutePath());
    task.systemProperty("hermes.game.sources.dir", context.gameProject().file("src/main/java").getAbsolutePath());
    task.systemProperty("hermes.game.title", gameConfig.getTitle());
    task.systemProperty("hermes.game.scene", gameConfig.getScene());
  }

  record ProjectHermesContext(
      org.gradle.api.Project gameProject,
      HermesConfig config,
      String applicationClass,
      HermesGameConfig gameConfig) {

    static ProjectHermesContext of(org.gradle.api.Project gameProject) {
      HermesConfig config = HermesConfig.resolve(gameProject);
      return new ProjectHermesContext(
          gameProject,
          config,
          config.getGame().getApplicationClass(),
          HermesGameConfigs.parse(gameProject));
    }
  }
}
