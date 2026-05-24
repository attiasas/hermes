package dev.hermes.gradle.internal;

import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.tooling.launch.LaunchMode;
import dev.hermes.tooling.launch.RuntimeConfigWriter;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

/**
 * Writes {@code hermes-runtime.properties} for packaged runtime configuration.
 */
public final class HermesRuntimeConfigGenerator {

    private HermesRuntimeConfigGenerator() {
    }

    public static void write(Project gameProject, HermesExtension extension, File outputDir) {
        LaunchMode mode =
                HermesDistributionMode.isDistributionExport(gameProject)
                        ? LaunchMode.DISTRIBUTION_EXPORT
                        : LaunchMode.DEV;
        Map<String, String> props = LaunchConfigGradle.resolveFromParts(
                        extension,
                        HermesGameConfigs.parse(gameProject),
                        HermesPlatforms.resolve(gameProject),
                        mode)
                .asMap();
        try {
            RuntimeConfigWriter.write(outputDir, props);
        } catch (IOException e) {
            throw new GradleException("Failed to write hermes-runtime.properties", e);
        }
    }
}
