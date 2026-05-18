package dev.hermes.cli;

import java.nio.file.Path;
import java.util.Locale;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "new", description = "Create a new Hermes game project from a template")
public final class NewCommand implements Runnable {

  @Parameters(index = "0", description = "Target directory for the new project")
  Path targetDir;

  @Option(names = "--name", description = "Project display name (default: directory name)")
  String name;

  @Option(names = "--package", description = "Java package (default: dev.hermes.<dir>)")
  String packageName;

  @Option(names = "--template", defaultValue = "empty", description = "Template id (only 'empty' for now)")
  String template;

  @Option(
      names = "--engine-version",
      defaultValue = "0.1.0-SNAPSHOT",
      description = "Hermes engine version for Maven coordinates")
  String engineVersion;

  @Override
  public void run() {
    if (!"empty".equals(template)) {
      System.err.println("Unknown template: " + template + " (supported: empty)");
      throw new picocli.CommandLine.ParameterException(
          new picocli.CommandLine(NewCommand.class), "Unknown template: " + template);
    }
    String projectName = name != null ? name : targetDir.getFileName().toString();
    String pkg =
        packageName != null
            ? packageName
            : "dev.hermes." + projectName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    try {
      TemplateSupport.materializeEmptyTemplate(targetDir.toAbsolutePath(), projectName, pkg, engineVersion);
      System.out.println("Created Hermes project at " + targetDir.toAbsolutePath());
      System.out.println("Next:");
      System.out.println("  cd " + targetDir);
      System.out.println("  ./gradlew :game:hermesDoctor");
      System.out.println("  ./gradlew :game:hermesRunDesktop");
    } catch (Exception e) {
      System.err.println("Failed to create project: " + e.getMessage());
      throw new picocli.CommandLine.ExecutionException(
          new picocli.CommandLine(NewCommand.class), "Failed to create project", e);
    }
  }
}
