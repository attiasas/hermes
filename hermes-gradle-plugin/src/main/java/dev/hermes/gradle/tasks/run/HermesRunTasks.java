package dev.hermes.gradle.tasks.run;

import dev.hermes.gradle.android.HermesAndroidLauncherConfigurer;
import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.gradle.html.TeaLauncherGenerator;
import dev.hermes.gradle.internal.HermesGameConfigs;
import dev.hermes.gradle.internal.HermesJvmArgs;
import dev.hermes.gradle.internal.HermesPlatforms;
import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.launch.HermesLaunchProperties;
import dev.hermes.tooling.platform.DesktopPlatform;
import dev.hermes.tooling.platform.HtmlPlatform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;

/**
 * Desktop, HTML, and Android run task wiring for {@code :game}.
 */
public final class HermesRunTasks {

    private HermesRunTasks() {
    }

    public static void wireAfterEvaluate(Project project, HermesExtension extension, File assetsDir) {
        wireDesktopRun(project, extension, assetsDir);
        wireHtmlRun(project, extension, assetsDir);
        wireAndroidRun(project, extension);
        project
                .getGradle()
                .projectsEvaluated(gradle -> wireLauncherAssets(project.getRootProject(), assetsDir));
    }

    public static void registerMissingLauncherTask(
            Project project,
            String taskName,
            String platformName,
            String launcherModule,
            String settingsFile) {
        project
                .getTasks()
                .register(
                        taskName,
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Requires " + launcherModule + " (enable in " + settingsFile + ")");
                            task.doLast(
                                    t -> {
                                        throw new GradleException(
                                                "Platform '"
                                                        + platformName
                                                        + "' is enabled in "
                                                        + settingsFile
                                                        + " but "
                                                        + launcherModule
                                                        + " is not included. "
                                                        + "Set hermes { platforms { "
                                                        + platformName
                                                        + " { enabled true } } } in "
                                                        + settingsFile
                                                        + " and sync Gradle.");
                                    });
                        });
    }

    private static void wireLauncherAssets(Project root, File assetsDir) {
        Project desktop = root.findProject("hermes-launcher-desktop");
        if (desktop != null) {
            desktop.getTasks().named("run", JavaExec.class).configure(task -> task.setWorkingDir(assetsDir));
        }
    }

    private static void wireDesktopRun(Project project, HermesExtension extension, File assetsDir) {
        Project root = project.getRootProject();
        Project launcher = root.findProject("hermes-launcher-desktop");
        if (launcher == null) {
            if (HermesPlatforms.resolve(project).getDesktop().isEnabled()) {
                registerMissingLauncherTask(
                        project, "hermesRunDesktop", "desktop", "hermes-launcher-desktop", "settings.gradle");
            }
            return;
        }
        project
                .getTasks()
                .register(
                        "hermesRunDesktop",
                        JavaExec.class,
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Run the game on desktop (LWJGL3)");
                            task.dependsOn(
                                    launcher.getTasks().named("classes"),
                                    project.getTasks().named("classes"),
                                    "generateAssetList");
                            task.getMainClass().set("dev.hermes.launcher.desktop.Lwjgl3Launcher");
                            SourceSet launcherMain =
                                    launcher.getExtensions().getByType(SourceSetContainer.class).getByName("main");
                            SourceSet gameMain =
                                    project.getExtensions().getByType(SourceSetContainer.class).getByName("main");
                            task.setClasspath(launcherMain.getRuntimeClasspath().plus(gameMain.getRuntimeClasspath()));
                            task.setWorkingDir(assetsDir);
                            JavaToolchainService toolchains = project.getExtensions().getByType(JavaToolchainService.class);
                            task.getJavaLauncher()
                                    .set(toolchains.launcherFor(spec -> spec.getLanguageVersion().set(JavaLanguageVersion.of(17))));
                            applyDesktopJvmArgs(task);
                            applyDesktopSystemProperties(task, project, extension);
                            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
                            if (os.contains("mac")) {
                                task.jvmArgs("-XstartOnFirstThread");
                            }
                            task.environment("__GL_THREADED_OPTIMIZATIONS", "0");
                        });
    }

    private static void applyDesktopJvmArgs(JavaExec task) {
        task.doFirst(
                t -> {
                    List<String> args = new ArrayList<>(task.getJvmArgs());
                    HermesJvmArgs.stripNativeAccess(args);
                    if (HermesJvmArgs.supportsNativeAccess(task)) {
                        args.add(HermesJvmArgs.NATIVE_ACCESS_FLAG);
                    }
                    task.setJvmArgs(args);
                });
    }

    private static void applyDesktopSystemProperties(
            JavaExec task, Project gameProject, HermesExtension extension) {
        DesktopPlatform desktop = HermesPlatforms.resolve(gameProject).getDesktop();
        HermesGameConfig config = HermesGameConfigs.parse(gameProject);
        List<String> jvmArgs = new ArrayList<>(task.getJvmArgs());
        HermesJvmArgs.stripNativeAccess(jvmArgs);
        HermesLaunchProperties.Builder launch =
                HermesLaunchProperties.builder()
                        .applicationClass(extension.getApplicationClass())
                        .debug(extension.isDebug())
                        .windowTitle(config.getTitle())
                        .windowSize(desktop.getWidth(), desktop.getHeight())
                        .scene(config.getScene())
                        .renderPipeline(config.getRenderPipeline())
                        .desktopVsync(desktop.isVsync())
                        .desktopResizable(desktop.isResizable())
                        .desktopForegroundFps(desktop.getForegroundFps())
                        .desktopGradleRun();
        jvmArgs.addAll(launch.build().toJvmArgs());
        task.setJvmArgs(jvmArgs);
    }

    private static void wireHtmlRun(Project project, HermesExtension extension, File assetsDir) {
        Project root = project.getRootProject();
        Project launcher = root.findProject("hermes-launcher-html");
        if (launcher == null) {
            if (HermesPlatforms.resolve(project).getHtml().isEnabled()) {
                registerMissingLauncherTask(
                        project,
                        "hermesRunHtml",
                        "html",
                        "hermes-launcher-html",
                        "settings.gradle");
            }
            return;
        }
        registerGenerateTeaLauncher(project, extension, launcher);
        wireHtmlBuildTasks(project, extension, launcher, assetsDir);
        project
                .getTasks()
                .register(
                        "hermesRunHtml",
                        JavaExec.class,
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Run the game in a local HTML/TeaVM dev server at http://localhost:8080/");
                            task.dependsOn(
                                    "generateTeaLauncher",
                                    launcher.getTasks().named("classes"),
                                    project.getTasks().named("classes"),
                                    "generateAssetList");
                            task.getMainClass().set("dev.hermes.launcher.html.TeaVMBuilder");
                            SourceSet launcherMain =
                                    launcher.getExtensions().getByType(SourceSetContainer.class).getByName("main");
                            SourceSet gameMain =
                                    project.getExtensions().getByType(SourceSetContainer.class).getByName("main");
                            task.setClasspath(launcherMain.getRuntimeClasspath().plus(gameMain.getRuntimeClasspath()));
                            task.setWorkingDir(launcher.getProjectDir());
                            task.args("run");
                            applyHtmlSystemProperties(task, project, extension, assetsDir);
                        });
    }

    private static void wireHtmlBuildTasks(
            Project gameProject, HermesExtension extension, Project htmlLauncher, File assetsDir) {
        htmlLauncher
                .getTasks()
                .withType(JavaExec.class)
                .configureEach(
                        task -> {
                            String name = task.getName();
                            if (!name.equals("buildRelease") && !name.equals("runRelease") && !name.equals("runDebug")) {
                                return;
                            }
                            task.dependsOn(gameProject.getTasks().named("generateTeaLauncher"));
                            applyHtmlSystemProperties(task, gameProject, extension, assetsDir);
                        });
    }

    private static void registerGenerateTeaLauncher(
            Project gameProject, HermesExtension extension, Project htmlLauncher) {
        java.io.File outputDir = htmlLauncher.file("build/generated-tea-sources");
        gameProject
                .getTasks()
                .register(
                        "generateTeaLauncher",
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Generate TeaVM HTML entry class for the configured application");
                            task.getInputs().property("applicationClass", extension.getApplicationClass());
                            task.getOutputs().dir(outputDir);
                            task.doLast(
                                    t -> {
                                        try {
                                            TeaLauncherGenerator.write(outputDir.toPath(), extension.getApplicationClass());
                                        } catch (java.io.IOException e) {
                                            throw new GradleException("Failed to generate TeaVM launcher sources", e);
                                        }
                                    });
                        });
        htmlLauncher
                .getPluginManager()
                .withPlugin(
                        "java-library",
                        applied ->
                                htmlLauncher
                                        .getTasks()
                                        .named("compileJava")
                                        .configure(t -> t.dependsOn(gameProject.getTasks().named("generateTeaLauncher"))));
    }

    private static void applyHtmlSystemProperties(
            JavaExec task, Project gameProject, HermesExtension extension, File assetsDir) {
        HtmlPlatform html = HermesPlatforms.resolve(gameProject).getHtml();
        HermesGameConfig config = HermesGameConfigs.parse(gameProject);
        HermesLaunchProperties props =
                HermesLaunchProperties.builder()
                        .applicationClass(extension.getApplicationClass())
                        .debug(extension.isDebug())
                        .windowTitle(config.getTitle())
                        .windowSize(html.getWidth(), html.getHeight())
                        .scene(config.getScene())
                        .renderPipeline(config.getRenderPipeline())
                        .htmlDevServerPort(html.getDevServerPort())
                        .htmlWebAssembly(html.isWebAssembly())
                        .assetsDir(assetsDir.getAbsolutePath())
                        .gameSourcesDir(gameProject.file("src/main/java").getAbsolutePath())
                        .build();
        for (var entry : props.asMap().entrySet()) {
            task.systemProperty(entry.getKey(), entry.getValue());
        }
    }

    private static void wireAndroidRun(Project project, HermesExtension extension) {
        Project root = project.getRootProject();
        Project launcher = root.findProject("hermes-launcher-android");
        if (launcher == null) {
            if (HermesPlatforms.resolve(project).getAndroid().isEnabled()) {
                registerMissingLauncherTask(
                        project,
                        "hermesRunAndroid",
                        "android",
                        "hermes-launcher-android",
                        "settings.gradle");
            }
            return;
        }
        HermesAndroidLauncherConfigurer.wire(project, launcher, extension);
        project
                .getTasks()
                .register(
                        "hermesRunAndroid",
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Install and launch the Android build on a connected device");
                            task.dependsOn(
                                    ":hermes-launcher-android:installDebug",
                                    ":hermes-launcher-android:run",
                                    "generateAssetList");
                        });
    }
}
