package dev.hermes.gradle;

import java.io.File;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Zip;

final class HermesZipExport {

  private HermesZipExport() {}

  static Zip register(
      Project owner, String taskName, File inputDir, File outputZip, String description) {
    return owner
        .getTasks()
        .register(
            taskName,
            Zip.class,
            zip -> {
              zip.setGroup("hermes");
              zip.setDescription(description);
              zip.from(inputDir);
              zip.getDestinationDirectory().set(outputZip.getParentFile());
              zip.getArchiveFileName().set(outputZip.getName());
              zip.doLast(
                  t -> {
                    if (!outputZip.isFile() || outputZip.length() < 512) {
                      throw new org.gradle.api.GradleException(
                          "Export zip missing or too small: " + outputZip.getAbsolutePath());
                    }
                    owner.getLogger().lifecycle("Hermes export: {}", outputZip.getAbsolutePath());
                  });
            })
        .get();
  }
}
