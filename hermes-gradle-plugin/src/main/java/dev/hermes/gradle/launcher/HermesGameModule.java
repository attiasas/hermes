package dev.hermes.gradle.launcher;

import dev.hermes.gradle.dsl.HermesConfig;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public final class HermesGameModule {

    private HermesGameModule() {}

    public static String resolveName(Project rootProject) {
        var extra = rootProject.getExtensions().getExtraProperties();
        if (!extra.has(HermesConfig.GAME_MODULE_PROPERTY)) {
            throw new GradleException(
                    "hermes.gameModule is required in settings.gradle, e.g. hermes { gameModule = 'game' }");
        }
        String name = extra.get(HermesConfig.GAME_MODULE_PROPERTY).toString().trim();
        if (name.isEmpty()) {
            throw new GradleException("hermes.gameModule must not be blank");
        }
        return name;
    }

    public static Project resolveProject(Project fromProject) {
        String name = resolveName(fromProject.getRootProject());
        Project game = fromProject.getRootProject().findProject(":" + name);
        if (game == null) {
            throw new GradleException("Hermes game module ':" + name + "' is not included in settings.gradle");
        }
        return game;
    }

    public static String taskPath(String taskName, Project fromProject) {
        return ":" + resolveName(fromProject.getRootProject()) + ":" + taskName;
    }
}
