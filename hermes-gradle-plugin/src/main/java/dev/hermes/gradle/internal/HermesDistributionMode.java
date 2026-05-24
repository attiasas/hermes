package dev.hermes.gradle.internal;

import dev.hermes.gradle.internal.HermesGameConfigs;
import dev.hermes.gradle.internal.HermesPlatforms;
import dev.hermes.gradle.dsl.HermesConfig;
import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.tooling.launch.HermesLaunchProperties;
import dev.hermes.tooling.launch.LaunchMode;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.tasks.JavaExec;

/**
 * Forces distribution (non-debug) flags on export-related tasks without mutating {@link HermesExtension}.
 */
public final class HermesDistributionMode {

    static final String EXPORT_ACTIVE_PROPERTY = "hermesDistributionExportActive";

    private HermesDistributionMode() {
    }

    public static void registerExportGraphListener(org.gradle.api.Project gameProject) {
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
        dev.hermes.gradle.internal.HermesJvmArgs.stripNativeAccess(jvmArgs);
        jvmArgs.addAll(
                LaunchConfigGradle.resolve(context.gameProject(), LaunchMode.DISTRIBUTION_EXPORT)
                        .toJvmArgs());
        task.setJvmArgs(jvmArgs);
    }

    public static void applyHtml(JavaExec task, ProjectHermesContext context, java.io.File assetsDir) {
        HermesLaunchProperties base =
                LaunchConfigGradle.resolve(context.gameProject(), LaunchMode.DISTRIBUTION_EXPORT);
        java.io.File runtimeDir = context.gameProject().file("build/generated/hermes-runtime");
        HermesLaunchProperties props =
                HermesLaunchProperties.builder()
                        .putAll(base.asMap())
                        .runtimeConfigDir(runtimeDir.getAbsolutePath())
                        .assetsDir(assetsDir.getAbsolutePath())
                        .gameSourcesDir(context.gameProject().file("src/main/java").getAbsolutePath())
                        .build();
        for (var entry : props.asMap().entrySet()) {
            task.systemProperty(entry.getKey(), entry.getValue());
        }
    }

    public record ProjectHermesContext(
            org.gradle.api.Project gameProject,
            HermesConfig config,
            String applicationClass,
            dev.hermes.tooling.config.HermesGameConfig gameConfig) {

        public static ProjectHermesContext of(org.gradle.api.Project gameProject) {
            HermesConfig config = HermesConfig.resolve(gameProject);
            return new ProjectHermesContext(
                    gameProject,
                    config,
                    config.getGame().getApplicationClass(),
                    HermesGameConfigs.parse(gameProject));
        }
    }
}
