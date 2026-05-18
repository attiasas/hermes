package dev.hermes.cli;

import dev.hermes.tooling.HermesHomeResolver;
import java.io.File;
import java.nio.file.Path;

/** Locates a Hermes engine checkout for writing {@code hermes.home} into new projects. */
final class HermesHomeDetector {

  private static final int MAX_PARENT_LEVELS = 8;

  private HermesHomeDetector() {}

  static File detect(Path projectDir) {
    File fromResolver = HermesHomeResolver.resolve(projectDir);
    if (HermesHomeResolver.isHermesCheckout(fromResolver)) {
      return fromResolver;
    }
    File fromProject = walkParents(projectDir.toAbsolutePath());
    if (fromProject != null) {
      return fromProject;
    }
    return walkParents(Path.of(System.getProperty("user.dir")).toAbsolutePath());
  }

  private static File walkParents(Path start) {
    Path current = start;
    for (int i = 0; i < MAX_PARENT_LEVELS && current != null; i++) {
      File candidate = current.toFile();
      if (HermesHomeResolver.isHermesCheckout(candidate)) {
        return candidate.getAbsoluteFile();
      }
      current = current.getParent();
    }
    return null;
  }
}
