package dev.hermes.gradle.export;

import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

/** Helpers for export staging directories. */
final class HermesExportStaging {

  private HermesExportStaging() {}

  static void cleanBeforeCopy(Project project, File stagingDir) {
    if (stagingDir == null) {
      return;
    }
    if (stagingDir.exists()) {
      project.delete(stagingDir);
    }
    if (!stagingDir.mkdirs() && !stagingDir.isDirectory()) {
      throw new GradleException("Failed to create staging directory " + stagingDir.getAbsolutePath());
    }
  }
}
