package dev.hermes.gradle.launcher;

import dev.hermes.gradle.internal.HermesDependencyResolver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class HermesDesktopLauncherPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply("application");
        project.getPlugins().apply("io.github.fourlastor.construo");
        LauncherScript.apply(project, HermesDesktopLauncherPlugin.class, "/launcher/desktop.gradle");
        HermesDependencyResolver.wireLauncherDependencies(project, "hermes-launcher-desktop");
    }
}
