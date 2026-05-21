package dev.hermes.gradle;

import dev.hermes.gradle.dsl.HermesConfig;
import dev.hermes.gradle.dsl.HermesExtension;
import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;

/** Wires {@code hermes-api} / {@code hermes-core} from sibling projects, HERMES_HOME, or Maven. */
public final class HermesDependencyResolver {

  private HermesDependencyResolver() {}

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
    gameProject.getRepositories().mavenLocal();
    gameProject.getRepositories().mavenCentral();
  }
}
