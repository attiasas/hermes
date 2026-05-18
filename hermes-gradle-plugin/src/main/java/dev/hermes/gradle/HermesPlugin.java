package dev.hermes.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;

/** Project plugin for the user {@code :game} module. */
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

    registerAssetListTask(project);
    registerRuntimeConfigTask(project);
    registerSyncPlatformsTask(project);
    registerHermesDoctorTask(project);

    project.afterEvaluate(
        evaluated -> {
          if (extension.getApplicationClass() == null || extension.getApplicationClass().isBlank()) {
            throw new GradleException(
                "hermes.applicationClass must be set in " + project.getPath() + "/build.gradle");
          }
          File assetsDir = HermesAssets.resolve(project, extension);
          project.getExtensions().add("hermesGameConfig", loadGameConfig(project));
          wireGameAssetResources(project, assetsDir);
          configureAssetListTask(project, assetsDir);
          wireDesktopRun(project, extension, assetsDir);
          wireHtmlRun(project, extension, assetsDir);
          wireAndroidRun(project, extension);
          project
              .getGradle()
              .projectsEvaluated(gradle -> wireLauncherAssets(project.getRootProject(), assetsDir));
        });
  }

  private static HermesGameConfig loadGameConfig(Project project) {
    return HermesGameConfigParser.parse(project.file("hermes.json"));
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
                    String engineVersion = HermesEngineVersion.resolve(gameProject, gameExtension);
                    File hermesHome = HermesHomeResolver.resolve(gameProject);
                    HermesPlatformSync.syncAllEnabled(
                        root.getRootDir(),
                        HermesPlatforms.resolve(gameProject),
                        engineVersion,
                        hermesHome);
                  });
            });
  }

  private static void registerHermesDoctorTask(Project project) {
    project.getTasks()
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

  private static void registerAssetListTask(Project project) {
    File generatedDir = project.file("build/generated/hermes-assets");
    project
        .getExtensions()
        .getByType(SourceSetContainer.class)
        .getByName("main")
        .getResources()
        .srcDir(generatedDir);

    project
        .getTasks()
        .register(
            "generateAssetList",
            task -> {
              task.setGroup("hermes");
              task.setDescription("Generate assets.txt from the game assets directory");
            });
    project.getTasks().named("processResources").configure(t -> t.dependsOn("generateAssetList"));
  }

  private static void configureAssetListTask(Project project, File assetsDir) {
    File generatedDir = project.file("build/generated/hermes-assets");
    File assetsFile = new File(generatedDir, "assets.txt");
    project
        .getTasks()
        .named(
            "generateAssetList",
            task -> {
              task.getInputs().dir(assetsDir);
              task.getOutputs().file(assetsFile);
              task.doLast(
                  t -> {
                    try {
                      if (!assetsDir.isDirectory()) {
                        return;
                      }
                      if (!generatedDir.exists() && !generatedDir.mkdirs()) {
                        throw new GradleException("Could not create " + generatedDir.getAbsolutePath());
                      }
                      if (assetsFile.exists() && !assetsFile.delete()) {
                        throw new GradleException("Could not delete " + assetsFile.getAbsolutePath());
                      }
                      collectAssetPaths(assetsDir, assetsDir, assetsFile);
                    } catch (java.io.IOException e) {
                      throw new GradleException("Failed to generate assets.txt", e);
                    }
                  });
            });
  }

  private static void collectAssetPaths(File root, File current, File assetsFile) throws java.io.IOException {
    File[] children = current.listFiles();
    if (children == null) {
      return;
    }
    java.util.Arrays.sort(children, java.util.Comparator.comparing(File::getName));
    for (File child : children) {
      if (child.getName().equals("assets.txt")) {
        continue;
      }
      if (child.isDirectory()) {
        collectAssetPaths(root, child, assetsFile);
      } else {
        String relative = root.toPath().relativize(child.toPath()).toString().replace('\\', '/');
        try (java.io.FileWriter writer = new java.io.FileWriter(assetsFile, true)) {
          writer.write(relative + "\n");
        }
      }
    }
  }

  private static void wireGameAssetResources(Project project, File assetsDir) {
    File resourcesRoot = project.file("src/main/resources");
    if (!isUnderDirectory(assetsDir, resourcesRoot)) {
      project
          .getExtensions()
          .getByType(SourceSetContainer.class)
          .getByName("main")
          .getResources()
          .srcDir(assetsDir);
    }
  }

  private static boolean isUnderDirectory(File child, File parent) {
    try {
      return child.getCanonicalFile().toPath().startsWith(parent.getCanonicalFile().toPath());
    } catch (java.io.IOException e) {
      return false;
    }
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
                  .set(toolchains.launcherFor(spec -> spec.getLanguageVersion().set(JavaLanguageVersion.of(11))));
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
          task.setJvmArgs(args);
        });
  }

  private static void applyDesktopSystemProperties(
      JavaExec task, Project gameProject, HermesExtension extension) {
    DesktopPlatformSpec desktop = HermesPlatforms.resolve(gameProject).getDesktop();
    HermesGameConfig config = HermesGameConfigParser.parse(gameProject.file("hermes.json"));
    List<String> jvmArgs = new ArrayList<>(task.getJvmArgs());
    HermesJvmArgs.stripNativeAccess(jvmArgs);
    jvmArgs.add("-Dhermes.applicationClass=" + extension.getApplicationClass());
    jvmArgs.add("-Dhermes.debug=" + extension.isDebug());
    jvmArgs.add("-Dhermes.window.width=" + desktop.getWidth());
    jvmArgs.add("-Dhermes.window.height=" + desktop.getHeight());
    jvmArgs.add("-Dhermes.window.title=" + config.getTitle());
    jvmArgs.add("-Dhermes.desktop.vsync=" + desktop.isVsync());
    jvmArgs.add("-Dhermes.desktop.resizable=" + desktop.isResizable());
    jvmArgs.add("-Dhermes.desktop.foregroundFps=" + desktop.getForegroundFps());
    jvmArgs.add("-Dhermes.game.title=" + config.getTitle());
    jvmArgs.add("-Dhermes.game.scene=" + config.getScene());
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
    HtmlPlatformSpec html = HermesPlatforms.resolve(gameProject).getHtml();
    HermesGameConfig config = HermesGameConfigParser.parse(gameProject.file("hermes.json"));
    task.systemProperty("hermes.applicationClass", extension.getApplicationClass());
    task.systemProperty("hermes.debug", String.valueOf(extension.isDebug()));
    task.systemProperty("hermes.window.width", String.valueOf(html.getWidth()));
    task.systemProperty("hermes.window.height", String.valueOf(html.getHeight()));
    task.systemProperty("hermes.window.title", config.getTitle());
    task.systemProperty("hermes.html.devServerPort", String.valueOf(html.getDevServerPort()));
    task.systemProperty("hermes.html.webAssembly", String.valueOf(html.isWebAssembly()));
    task.systemProperty("hermes.assets.dir", assetsDir.getAbsolutePath());
    task.systemProperty("hermes.game.sources.dir", gameProject.file("src/main/java").getAbsolutePath());
    task.systemProperty("hermes.game.title", config.getTitle());
    task.systemProperty("hermes.game.scene", config.getScene());
  }

  private static void registerMissingLauncherTask(
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
