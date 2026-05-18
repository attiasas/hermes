package dev.hermes.gradle;

import java.io.File;
import java.io.IOException;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;

final class HermesIconsConfigurer {

  private HermesIconsConfigurer() {}

  static void registerAndroidIconCopy(Project gameProject, Project androidLauncher) {
    if (androidLauncher == null) {
      return;
    }
    File generatedRes = gameProject.file("build/generated/hermes-icons/android-res");
    gameProject
        .getTasks()
        .register(
            "generateHermesAndroidIcons",
            task -> {
              task.setGroup("hermes");
              task.setDescription("Generate Android mipmap launcher icons from the game icon");
              task.getOutputs().dir(generatedRes);
              task.doLast(
                  t -> {
                    try {
                      HermesExtension extension = gameProject.getExtensions().getByType(HermesExtension.class);
                      File icon = HermesIcons.androidLauncher(gameProject, extension);
                      HermesAndroidIconGenerator.generateMipmaps(icon, generatedRes);
                    } catch (IOException e) {
                      throw new GradleException("Failed to generate Android launcher icon", e);
                    }
                  });
            });
    wireAndroidGeneratedRes(androidLauncher, generatedRes);
    androidLauncher
        .getTasks()
        .named("preBuild")
        .configure(task -> task.dependsOn(gameProject.getTasks().named("generateHermesAndroidIcons")));
  }

  private static void wireAndroidGeneratedRes(Project androidLauncher, File generatedRes) {
    Object androidExt = androidLauncher.getExtensions().findByName("android");
    if (androidExt == null) {
      return;
    }
    try {
      Object sourceSets = androidExt.getClass().getMethod("getSourceSets").invoke(androidExt);
      Object main = sourceSets.getClass().getMethod("getByName", String.class).invoke(sourceSets, "main");
      Object res = main.getClass().getMethod("getRes").invoke(main);
      res.getClass().getMethod("srcDir", Object.class).invoke(res, generatedRes);
    } catch (ReflectiveOperationException e) {
      throw new GradleException("Failed to wire generated Android icon res", e);
    }
  }

  static Copy registerHtmlFaviconCopy(
      Project gameProject, Project htmlLauncher, File distDir, HermesExtension extension) {
    File favicon = HermesIcons.webFavicon(gameProject, extension);
    return gameProject
        .getTasks()
        .register(
            "copyHermesHtmlFavicon",
            Copy.class,
            copy -> {
              copy.setGroup("hermes");
              copy.setDescription("Copy favicon into HTML export output");
              copy.from(favicon);
              copy.into(distDir);
              copy.rename(s -> "favicon.png");
            })
        .get();
  }
}
