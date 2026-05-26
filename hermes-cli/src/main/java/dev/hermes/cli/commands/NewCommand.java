package dev.hermes.cli.commands;

import dev.hermes.cli.template.TemplateSupport;
import dev.hermes.tooling.project.GameModuleNames;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "new", description = "Create a new Hermes game project from a template")
public final class NewCommand implements Runnable {

    private static final Set<String> SUPPORTED_TEMPLATES = Set.of("minimal", "2d", "multi-scene");

    @Parameters(index = "0", description = "Target directory for the new project")
    Path targetDir;

    @Option(names = "--name", description = "Project display name (default: directory name)")
    String name;

    @Option(names = "--package", description = "Java package (default: dev.hermes.<dir>)")
    String packageName;

    @Option(
            names = "--template",
            defaultValue = "minimal",
            description = "Template id: minimal (3D, default), 2d (orthographic sprites), or multi-scene")
    String template;

    @Option(
            names = "--engine-version",
            defaultValue = "0.1.0-SNAPSHOT",
            description = "Hermes engine version for Maven coordinates")
    String engineVersion;

    @Option(
            names = "--platforms",
            defaultValue = "desktop",
            description = "Comma-separated platforms to enable: desktop, html, android")
    String platforms;

    @Option(
            names = "--android-sdk",
            description = "Android SDK path (used when android is in --platforms)")
    Path androidSdk;

    @Option(
            names = "--module",
            description = "Gradle subproject name for the game module (default: game)")
    String module;

    @Override
    public void run() {
        if (!SUPPORTED_TEMPLATES.contains(template)) {
            System.err.println("Unknown template: " + template + " (supported: minimal, 2d, multi-scene)");
            throw new picocli.CommandLine.ParameterException(
                    new picocli.CommandLine(NewCommand.class), "Unknown template: " + template);
        }
        String projectName = name != null ? name : targetDir.getFileName().toString();
        String pkg =
                packageName != null
                        ? packageName
                        : "dev.hermes." + projectName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        Set<String> enabledPlatforms;
        try {
            String gameModule = GameModuleNames.normalize(module);
            enabledPlatforms = TemplateSupport.parsePlatforms(platforms);
            TemplateSupport.materializeTemplate(
                    template,
                    targetDir.toAbsolutePath(),
                    projectName,
                    pkg,
                    engineVersion,
                    enabledPlatforms,
                    androidSdk,
                    gameModule);
            String gradleModule = ":" + gameModule + ":";
            System.out.println("Created Hermes project at " + targetDir.toAbsolutePath());
            System.out.println("Next:");
            System.out.println("  cd " + targetDir);
            System.out.println("  ./gradlew " + gradleModule + "hermesDoctor");
            if (enabledPlatforms.contains("desktop")) {
                System.out.println("  ./gradlew " + gradleModule + "hermesRunDesktop");
            }
            if (enabledPlatforms.contains("html")) {
                System.out.println("  ./gradlew " + gradleModule + "hermesRunHtml");
            }
            if (enabledPlatforms.contains("android")) {
                System.out.println("  ./gradlew " + gradleModule + "hermesRunAndroid");
                if (androidSdk == null) {
                    System.out.println(
                            "  (Android: add sdk.dir to local.properties, or pass --android-sdk / set ANDROID_SDK_ROOT)");
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            throw new picocli.CommandLine.ParameterException(
                    new picocli.CommandLine(NewCommand.class), e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Failed to create project: " + e.getMessage());
            throw new picocli.CommandLine.ExecutionException(
                    new picocli.CommandLine(NewCommand.class), "Failed to create project", e);
        }
    }
}
