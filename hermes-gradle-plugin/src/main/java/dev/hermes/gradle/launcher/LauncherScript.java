package dev.hermes.gradle.launcher;

import java.net.URL;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

public final class LauncherScript {

    private LauncherScript() {}

    public static void apply(Project project, Class<?> anchor, String resourcePath) {
        URL url = anchor.getResource(resourcePath);
        if (url == null) {
            throw new GradleException("Missing launcher script: " + resourcePath);
        }
        try {
            project.apply(spec -> spec.from(url));
        } catch (Exception e) {
            throw new GradleException("Failed to apply launcher script " + resourcePath, e);
        }
    }
}
