package dev.hermes.gradle.launcher;

import dev.hermes.gradle.internal.HermesDependencyResolver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class HermesAndroidLauncherPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        LauncherScript.apply(project, HermesAndroidLauncherPlugin.class, "/launcher/android.gradle");
        HermesDependencyResolver.wireLauncherDependencies(project, "hermes-launcher-android");
    }
}
