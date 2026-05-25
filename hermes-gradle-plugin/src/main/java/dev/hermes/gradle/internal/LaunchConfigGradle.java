package dev.hermes.gradle.internal;

import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.launch.HermesLaunchProperties;
import dev.hermes.tooling.launch.LaunchConfigRequest;
import dev.hermes.tooling.launch.LaunchConfigResolver;
import dev.hermes.tooling.launch.LaunchMode;
import dev.hermes.tooling.platform.Platforms;
import org.gradle.api.Project;

public final class LaunchConfigGradle {

    private LaunchConfigGradle() {}

    public static HermesLaunchProperties resolve(Project gameProject, LaunchMode mode) {
        HermesExtension extension = gameProject.getExtensions().getByType(HermesExtension.class);
        HermesGameConfig game = HermesGameConfigs.parse(gameProject);
        Platforms platforms = HermesPlatforms.resolve(gameProject);
        return resolveFromParts(extension, game, platforms, mode);
    }

    public static HermesLaunchProperties resolveFromParts(
            HermesExtension extension,
            HermesGameConfig game,
            Platforms platforms,
            LaunchMode mode) {
        boolean export = mode == LaunchMode.DISTRIBUTION_EXPORT;
        boolean debug = export ? false : extension.isDebug();
        String minLevel = extension.getLogging().resolveMinLevel(debug, export);

        LaunchConfigRequest request =
                new LaunchConfigRequest(
                        extension.getApplicationClass(),
                        extension.isDebug(),
                        mode,
                        minLevel,
                        extension.getLogging().getPatternType(),
                        extension.getLogging().getPatterns(),
                        extension.getRuntime().asMap(),
                        game,
                        platforms);

        return LaunchConfigResolver.resolve(request);
    }
}
