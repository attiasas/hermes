package dev.hermes.tooling.gradle;

import java.io.File;
import java.nio.file.Path;

public final class HermesHomeResolver {

    public static final String ENV_HERMES_HOME = "HERMES_HOME";
    public static final String GRADLE_PROPERTY = "hermes.home";

    private HermesHomeResolver() {
    }

    public static File resolve(Path projectDir) {
        return resolve(projectDir, null, null);
    }

    /**
     * Resolves Hermes engine checkout from optional overrides, then {@code hermes.home} in
     * {@code gradle.properties}, then {@code HERMES_HOME}.
     */
    public static File resolve(Path projectDir, File configuredHome, String gradlePropertyOverride) {
        if (configuredHome != null && configuredHome.isDirectory()) {
            return configuredHome.getAbsoluteFile();
        }
        String fromProps =
                projectDir == null
                        ? null
                        : GradlePropertySupport.firstNonBlank(
                        gradlePropertyOverride,
                        GradlePropertySupport.readProperty(projectDir, GRADLE_PROPERTY));
        String fromEnv = System.getenv(ENV_HERMES_HOME);
        String path = GradlePropertySupport.firstNonBlank(fromProps, fromEnv);
        if (path == null || path.isBlank()) {
            return null;
        }
        File home = new File(path);
        return home.isDirectory() ? home.getAbsoluteFile() : null;
    }

    public static boolean isHermesCheckout(File home) {
        return home != null
                && home.isDirectory()
                && new File(home, "hermes-api").isDirectory()
                && new File(home, "hermes-core").isDirectory();
    }
}
