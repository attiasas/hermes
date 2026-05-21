package dev.hermes.gradle;

import dev.hermes.tooling.config.HermesGameConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Zip;

/** Registers {@code hermesExport*} tasks on {@code :game}. */
final class HermesExportTasks {

  private HermesExportTasks() {}

  static void register(Project gameProject) {
    HermesDistributionMode.registerExportGraphListener(gameProject);
    gameProject
        .getGradle()
        .projectsEvaluated(
            gradle -> {
              if (!gameProject.getPlugins().hasPlugin("dev.hermes")) {
                return;
              }
              HermesConfig config = HermesConfig.resolve(gameProject);
              File assetsDir = HermesAssets.resolve(gameProject, config.getGame());
              registerHtmlExport(gameProject, config, assetsDir);
              registerAndroidExport(gameProject, config);
              registerDesktopExport(gameProject, config);
              registerAggregate(gameProject, config);
            });
  }

  private static void registerAggregate(Project gameProject, HermesConfig config) {
    List<String> deps = new ArrayList<>();
    if (config.getPlatforms().getDesktop().isEnabled()) {
      deps.add("hermesExportDesktop");
    }
    if (config.getPlatforms().getHtml().isEnabled()) {
      deps.add("hermesExportHtml");
    }
    if (config.getPlatforms().getAndroid().isEnabled()) {
      deps.add("hermesExportAndroid");
    }
    if (deps.isEmpty()) {
      return;
    }
    gameProject
        .getTasks()
        .register(
            "hermesExport",
            task -> {
              task.setGroup("hermes");
              task.setDescription("Export distribution artifacts for all enabled platforms");
              task.dependsOn(deps);
            });
  }

  private static void registerHtmlExport(Project gameProject, HermesConfig config, File assetsDir) {
    if (!config.getPlatforms().getHtml().isEnabled()) {
      return;
    }
    Project launcher = gameProject.getRootProject().findProject("hermes-launcher-html");
    if (launcher == null) {
      HermesPlugin.registerMissingLauncherTaskStatic(
          gameProject, "hermesExportHtml", "html", "hermes-launcher-html", "settings.gradle");
      return;
    }
    File distRoot = gameProject.file("build/dist/html");
    File staging = new File(distRoot, "staging");
    File zipFile = new File(distRoot, HermesExportNaming.zipBaseName(gameProject, "html") + ".zip");

    SourceSet launcherMain =
        launcher.getExtensions().getByType(SourceSetContainer.class).getByName("main");
    SourceSet gameMain =
        gameProject.getExtensions().getByType(SourceSetContainer.class).getByName("main");

    launcher
        .getTasks()
        .named("buildRelease", JavaExec.class)
        .configure(
            build -> {
              build.setClasspath(launcherMain.getRuntimeClasspath().plus(gameMain.getRuntimeClasspath()));
              build.dependsOn(
                  gameProject.getTasks().named("classes"),
                  gameProject.getTasks().named("processResources"),
                  gameProject.getTasks().named("generateTeaLauncher"));
            });

    gameProject
        .getGradle()
        .getTaskGraph()
        .whenReady(
            graph -> {
              if (graph.hasTask(gameProject.getTasks().findByName("hermesExportHtml"))) {
                launcher
                    .getTasks()
                    .named("buildRelease", JavaExec.class)
                    .configure(
                        build ->
                            HermesDistributionMode.applyHtml(
                                build,
                                HermesDistributionMode.ProjectHermesContext.of(gameProject),
                                assetsDir));
              }
            });

    gameProject
        .getTasks()
        .register(
            "stageHermesExportHtml",
            Copy.class,
            copy -> {
              copy.setGroup("hermes");
              copy.setDescription("Stage HTML export output");
              copy.dependsOn(":hermes-launcher-html:buildRelease", "generateTeaLauncher", "classes", "generateAssetList");
              copy.doFirst(t -> HermesExportStaging.cleanBeforeCopy(gameProject, staging));
              copy.from(launcher.file("build/dist"));
              copy.into(staging);
              copy.doLast(t -> HermesHtmlExportBundle.writeInto(staging));
            });

    Zip zip =
        HermesZipExport.register(
            gameProject,
            "zipHermesExportHtml",
            staging,
            zipFile,
            "Zip HTML export as " + zipFile.getName());

    gameProject
        .getTasks()
        .register(
            "hermesExportHtml",
            task -> {
              task.setGroup("hermes");
              task.setDescription("Build and zip the HTML/TeaVM distribution");
              task.dependsOn(zip);
            });
    zip.dependsOn("stageHermesExportHtml");
  }

  private static void registerAndroidExport(Project gameProject, HermesConfig config) {
    if (!config.getPlatforms().getAndroid().isEnabled()) {
      return;
    }
    Project launcher = gameProject.getRootProject().findProject("hermes-launcher-android");
    if (launcher == null) {
      HermesPlugin.registerMissingLauncherTaskStatic(
          gameProject, "hermesExportAndroid", "android", "hermes-launcher-android", "settings.gradle");
      return;
    }
    HermesIconsConfigurer.registerAndroidIconCopy(gameProject, launcher);
    File distRoot = gameProject.file("build/dist/android");
    File staging = new File(distRoot, "staging");
    File zipFile = new File(distRoot, HermesExportNaming.zipBaseName(gameProject, "android") + ".zip");

    gameProject
        .getTasks()
        .register(
            "stageHermesExportAndroid",
            Copy.class,
            copy -> {
              copy.setGroup("hermes");
              copy.setDescription("Stage Android release APK");
              copy.dependsOn(
                  ":hermes-launcher-android:assembleRelease",
                  "generateAssetList",
                  "generateHermesAndroidIcons");
              copy.from(launcher.file("build/outputs/apk/release"));
              copy.include("*.apk");
              copy.into(staging);
            });

    Zip zip =
        HermesZipExport.register(
            gameProject,
            "zipHermesExportAndroid",
            staging,
            zipFile,
            "Zip Android export as " + zipFile.getName());
    zip.dependsOn("stageHermesExportAndroid");

    gameProject
        .getTasks()
        .register(
            "hermesExportAndroid",
            task -> {
              task.setGroup("hermes");
              task.setDescription("Build and zip the Android release APK (unsigned)");
              task.dependsOn(zip);
            });
  }

  private static void registerDesktopExport(Project gameProject, HermesConfig config) {
    if (!config.getPlatforms().getDesktop().isEnabled()) {
      return;
    }
    Project launcher = gameProject.getRootProject().findProject("hermes-launcher-desktop");
    if (launcher == null) {
      HermesPlugin.registerMissingLauncherTaskStatic(
          gameProject, "hermesExportDesktop", "desktop", "hermes-launcher-desktop", "settings.gradle");
      return;
    }
    configureDesktopLauncher(gameProject, launcher, config);
    DesktopPlatformSpec desktop = config.getPlatforms().getDesktop();
    List<String> targets = HermesDesktopExportTargets.forCurrentHost(desktop.getExportTargets());
    List<String> zipTasks = new ArrayList<>();
    File distRoot = gameProject.file("build/dist/desktop");
    for (String target : targets) {
      String packageTask = "package" + capitalize(target);
      if (launcher.getTasks().findByName(packageTask) == null) {
        gameProject.getLogger().warn("Hermes: skipping unknown Construo target '{}'", target);
        continue;
      }
      File staging = new File(distRoot, "staging/" + target);
      File zipFile =
          new File(
              distRoot,
              HermesExportNaming.zipBaseName(gameProject, HermesExportNaming.desktopZipSuffix(target))
                  + ".zip");
      String stageTaskName = "stageHermesExportDesktop" + capitalize(target);
      gameProject
          .getTasks()
          .register(
              stageTaskName,
              Copy.class,
              copy -> {
                copy.setGroup("hermes");
                copy.setDescription("Stage Construo output for " + target);
                copy.dependsOn(":hermes-launcher-desktop:" + packageTask, "classes", "generateAssetList");
                copy.doFirst(t -> HermesExportStaging.cleanBeforeCopy(gameProject, staging));
                copy.from(launcher.file("build/construo/" + target));
                copy.into(staging);
                copy.doLast(
                    t -> {
                      HermesDesktopExportFixup.fixStagingDirectory(staging);
                      HermesDesktopExportBundle.writeInto(staging);
                    });
              });
      String zipTaskName = "zipHermesExportDesktop" + capitalize(target);
      Zip zip =
          HermesZipExport.register(
              gameProject, zipTaskName, staging, zipFile, "Zip desktop export for " + target);
      zip.dependsOn(stageTaskName);
      zipTasks.add(zipTaskName);
    }
    if (zipTasks.isEmpty()) {
      return;
    }
    gameProject
        .getTasks()
        .register(
            "hermesExportDesktop",
            task -> {
              task.setGroup("hermes");
              task.setDescription("Build native desktop bundles and zip per target");
              task.dependsOn(zipTasks);
            });
  }

  private static void configureDesktopLauncher(Project gameProject, Project launcher, HermesConfig config) {
    HermesGameConfig gameConfig = HermesGameConfigs.parse(gameProject);
    DesktopPlatformSpec desktop = config.getPlatforms().getDesktop();
    String executableName = desktop.getExecutableName();
    if (executableName == null || executableName.isBlank()) {
      executableName = HermesExportNaming.sanitizeTitle(gameConfig.getTitle());
    }
    String bundleId = desktop.getBundleId();
    if (bundleId == null || bundleId.isBlank()) {
      bundleId = config.getPlatforms().getAndroid().getApplicationId();
    }
    if (bundleId == null || bundleId.isBlank()) {
      bundleId = "dev.hermes.game";
    }
    launcher.getExtensions().getExtraProperties().set("hermesConstruoName", executableName);
    launcher.getExtensions().getExtraProperties().set("hermesConstruoHumanName", gameConfig.getTitle());
    launcher.getExtensions().getExtraProperties().set("hermesConstruoBundleId", bundleId);
    launcher.getExtensions().getExtraProperties().set("hermesMacIcon", HermesIcons.desktopMac(gameProject, config.getGame()).getAbsolutePath());
    launcher.getExtensions().getExtraProperties().set("hermesWinIcon", HermesIcons.desktopWindows(gameProject, config.getGame()).getAbsolutePath());
    launcher.getExtensions().getExtraProperties().set("hermesProjectVersion", HermesExportNaming.version(gameProject));

    wireDesktopLauncherGameDependency(gameProject, launcher);
  }

  /** Ensures desktop export packages game classes and resources into the Construo fat JAR. */
  private static void wireDesktopLauncherGameDependency(Project gameProject, Project launcher) {
    launcher
        .getTasks()
        .matching(task -> task.getName().equals("jar") || task.getName().startsWith("package"))
        .configureEach(
            task ->
                task.dependsOn(
                    gameProject.getTasks().named("classes"),
                    gameProject.getTasks().named("processResources")));
  }

  private static String capitalize(String target) {
    if (target == null || target.isEmpty()) {
      return target;
    }
    return target.substring(0, 1).toUpperCase(Locale.ROOT) + target.substring(1);
  }
}
