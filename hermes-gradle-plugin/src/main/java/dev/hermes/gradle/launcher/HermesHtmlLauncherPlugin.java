package dev.hermes.gradle.launcher;

import dev.hermes.gradle.internal.HermesDependencyResolver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class HermesHtmlLauncherPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply("java-library");
        LauncherScript.apply(project, HermesHtmlLauncherPlugin.class, "/launcher/html.gradle");
        HermesDependencyResolver.wireLauncherDependencies(project, "hermes-launcher-html");
    }
}
