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

/** Project plugin for the user {@code :game} module. */
public final class HermesPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getPlugins().apply("java-library");
    HermesExtension extension = new HermesExtension();
    project.getExtensions().add("hermes", extension);

    project.getDependencies().add("api", project.project(":hermes-api"));
    project.getDependencies().add("runtimeOnly", project.project(":hermes-core"));

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

    project.afterEvaluate(
        evaluated -> {
          if (extension.getApplicationClass() == null || extension.getApplicationClass().isBlank()) {
            throw new GradleException(
                "hermes.applicationClass must be set in " + project.getPath() + "/build.gradle");
          }
          File assetsDir = HermesAssets.resolve(project, extension);
          project.getExtensions().add("hermesGameConfig", loadGameConfig(project));
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

  private static void wireLauncherAssets(Project root, File assetsDir) {
    root.getExtensions().getExtraProperties().set("hermesAssetsDir", assetsDir);

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
              applyDesktopJvmArgs(task, project, extension);
              String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
              if (os.contains("mac")) {
                task.jvmArgs("-XstartOnFirstThread");
              }
              task.environment("__GL_THREADED_OPTIMIZATIONS", "0");
            });
  }

  private static void applyDesktopJvmArgs(JavaExec task, Project gameProject, HermesExtension extension) {
    PlatformSpec desktop = extension.getPlatforms().getDesktop();
    HermesGameConfig config = HermesGameConfigParser.parse(gameProject.file("hermes.json"));
    List<String> jvmArgs = new ArrayList<>(task.getJvmArgs());
    jvmArgs.add("--enable-native-access=ALL-UNNAMED");
    jvmArgs.add("-Dhermes.applicationClass=" + extension.getApplicationClass());
    jvmArgs.add("-Dhermes.debug=" + extension.isDebug());
    jvmArgs.add("-Dhermes.window.width=" + desktop.getWidth());
    jvmArgs.add("-Dhermes.window.height=" + desktop.getHeight());
    jvmArgs.add("-Dhermes.window.title=" + desktop.getTitle());
    jvmArgs.add("-Dhermes.game.name=" + config.getName());
    jvmArgs.add("-Dhermes.game.scene=" + config.getScene());
    task.setJvmArgs(jvmArgs);
  }

  private static void wireHtmlRun(Project project, HermesExtension extension, File assetsDir) {
    Project root = project.getRootProject();
    Project launcher = root.findProject("hermes-launcher-html");
    if (launcher == null) {
      if (extension.getPlatforms().getHtml().isEnabled()) {
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
    PlatformSpec html = extension.getPlatforms().getHtml();
    task.systemProperty("hermes.applicationClass", extension.getApplicationClass());
    task.systemProperty("hermes.debug", String.valueOf(extension.isDebug()));
    task.systemProperty("hermes.window.width", String.valueOf(html.getWidth()));
    task.systemProperty("hermes.window.height", String.valueOf(html.getHeight()));
    task.systemProperty("hermes.window.title", html.getTitle());
    task.systemProperty("hermes.assets.dir", assetsDir.getAbsolutePath());
    HermesGameConfig config = HermesGameConfigParser.parse(gameProject.file("hermes.json"));
    task.systemProperty("hermes.game.name", config.getName());
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
                            + "' is enabled on "
                            + project.getPath()
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
      if (extension.getPlatforms().getAndroid().isEnabled()) {
        registerMissingLauncherTask(
            project,
            "hermesRunAndroid",
            "android",
            "hermes-launcher-android",
            "settings.gradle");
      }
      return;
    }
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
