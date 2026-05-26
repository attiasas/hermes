package dev.hermes.gradle.internal;

import dev.hermes.gradle.dsl.HermesConfig;
import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.gradle.launcher.HermesGameModule;

import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;

/**
 * Wires {@code hermes-api} / {@code hermes-core} from sibling projects, HERMES_HOME, or Maven.
 */
public final class HermesDependencyResolver {

    private HermesDependencyResolver() {
    }

    public static void wireGameDependencies(Project gameProject, HermesExtension extension) {
        DependencyHandler dependencies = gameProject.getDependencies();
        Project apiProject = gameProject.findProject(":hermes-api");
        Project coreProject = gameProject.findProject(":hermes-core");

        if (apiProject != null && coreProject != null) {
            dependencies.add("api", apiProject);
            dependencies.add("runtimeOnly", coreProject);
            return;
        }

        String version = HermesConfig.resolveEngineVersion(gameProject);
        dependencies.add("api", HermesEngineVersion.DEFAULT_GROUP + ":hermes-api:" + version);
        dependencies.add("runtimeOnly", HermesEngineVersion.DEFAULT_GROUP + ":hermes-core:" + version);
    }

    public static String mavenCoordinate(String artifact, String version) {
        return HermesEngineVersion.DEFAULT_GROUP + ":" + artifact + ":" + version;
    }

    public static boolean isCompileOnlyGameDependency(String launcherModuleName) {
        return "hermes-launcher-html".equals(launcherModuleName);
    }

    static String gameProjectPath(String gameModuleName) {
        return ":" + gameModuleName;
    }

    public static void wireLauncherDependencies(Project launcherProject, String launcherModuleName) {
        DependencyHandler deps = launcherProject.getDependencies();
        Project root = launcherProject.getRootProject();
        String engineVersion = HermesConfig.resolveEngineVersion(root);
        Project coreProject = root.findProject(":hermes-core");

        if (coreProject != null) {
            deps.add("implementation", coreProject);
        } else {
            deps.add("implementation", mavenCoordinate("hermes-core", engineVersion));
        }

        String config = isCompileOnlyGameDependency(launcherModuleName) ? "compileOnly" : "implementation";
        String gamePath = gameProjectPath(HermesGameModule.resolveName(root));
        deps.add(config, deps.project(Map.of("path", gamePath)));
    }
}
