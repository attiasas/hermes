package dev.hermes.gradle;

import dev.hermes.gradle.doctor.HermesDoctor;
import dev.hermes.gradle.dsl.HermesConfig;
import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.gradle.export.HermesExportTasks;
import dev.hermes.gradle.internal.GradleProjectEvaluate;
import dev.hermes.gradle.internal.HermesDependencyResolver;
import dev.hermes.gradle.internal.HermesGameConfigs;
import dev.hermes.gradle.internal.HermesHomeGradle;
import dev.hermes.gradle.internal.HermesJavaToolchain;
import dev.hermes.gradle.internal.HermesRuntimeConfigGenerator;
import dev.hermes.gradle.platform.HermesPlatformSync;
import dev.hermes.gradle.tasks.assets.HermesAssetTasks;
import dev.hermes.gradle.tasks.run.HermesRunTasks;
import dev.hermes.tooling.config.HermesGameConfig;

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Project plugin for the user {@code :game} module.
 */
public final class HermesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply("java-library");
        HermesJavaToolchain.applyJava11(project);
        HermesExtension extension = new HermesExtension();
        project.getExtensions().add("hermes", extension);

        HermesDependencyResolver.wireGameDependencies(project, extension);

        project
                .getTasks()
                .register(
                        "validateHermesJson",
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Parse and validate game hermes.json");
                            task.doLast(t -> loadGameConfig(project));
                        });
        project.getTasks().named("compileJava").configure(t -> t.dependsOn("validateHermesJson"));

        HermesAssetTasks.register(project);
        registerRuntimeConfigTask(project);
        registerSyncPlatformsTask(project);
        registerHermesDoctorTask(project);
        HermesExportTasks.register(project);

        GradleProjectEvaluate.whenEvaluated(
                project,
                evaluated -> {
                    if (extension.getApplicationClass() == null || extension.getApplicationClass().isBlank()) {
                        throw new GradleException(
                                "hermes.applicationClass must be set in " + project.getPath() + "/build.gradle");
                    }
                    File assetsDir = HermesAssetTasks.resolveAssetsDir(project, extension);
                    project.getExtensions().add("hermesGameConfig", loadGameConfig(project));
                    HermesAssetTasks.configureAfterEvaluate(project, extension, assetsDir);
                    HermesRunTasks.wireAfterEvaluate(project, extension, assetsDir);
                });
    }

    private static HermesGameConfig loadGameConfig(Project project) {
        return HermesGameConfigs.parse(project);
    }

    private static void registerSyncPlatformsTask(Project gameProject) {
        Project root = gameProject.getRootProject();
        if (root.getTasks().findByName("hermesSyncPlatforms") != null) {
            return;
        }
        root.getTasks()
                .register(
                        "hermesSyncPlatforms",
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Sync Hermes launcher platform stubs into .hermes/platforms/");
                            task.doLast(
                                    t -> {
                                        HermesExtension gameExtension =
                                                gameProject.getExtensions().getByType(HermesExtension.class);
                                        String engineVersion = HermesConfig.resolveEngineVersion(gameProject);
                                        File hermesHome = HermesHomeGradle.resolve(gameProject);
                                        HermesPlatformSync.syncAllEnabledForce(
                                                root.getRootDir(),
                                                HermesConfig.resolveSettingsPlatforms(gameProject),
                                                engineVersion,
                                                hermesHome);
                                    });
                        });
    }

    private static void registerHermesDoctorTask(Project project) {
        project
                .getTasks()
                .register(
                        "hermesDoctor",
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Validate Hermes project setup, toolchains, and forbidden libGDX imports");
                            task.doLast(t -> HermesDoctor.runGradle(project));
                        });
        project.getTasks().named("check").configure(t -> t.dependsOn("hermesDoctor"));
    }

    private static void registerRuntimeConfigTask(Project project) {
        File generatedDir = project.file("build/generated/hermes-runtime");
        project
                .getExtensions()
                .getByType(SourceSetContainer.class)
                .getByName("main")
                .getResources()
                .srcDir(generatedDir);

        project
                .getTasks()
                .register(
                        "generateHermesRuntimeConfig",
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Generate hermes-runtime.properties for all platforms");
                            task.getInputs().files(project.getBuildFile(), project.file("hermes.json"));
                            task.getOutputs().dir(generatedDir);
                            task.doLast(
                                    t -> {
                                        HermesExtension extension = project.getExtensions().getByType(HermesExtension.class);
                                        if (extension.getApplicationClass() == null || extension.getApplicationClass().isBlank()) {
                                            throw new GradleException(
                                                    "hermes.applicationClass must be set in " + project.getPath() + "/build.gradle");
                                        }
                                        HermesRuntimeConfigGenerator.write(project, extension, generatedDir);
                                    });
                        });

        project.getTasks().named("processResources").configure(t -> t.dependsOn("generateHermesRuntimeConfig"));
        project.getTasks().named("compileJava").configure(t -> t.dependsOn("generateHermesRuntimeConfig"));
    }
}
