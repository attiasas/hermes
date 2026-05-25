package dev.hermes.gradle.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Picks Construo desktop targets that can run on the current host (jlink is not cross-platform).
 */
public final class HermesDesktopExportTargets {

    private HermesDesktopExportTargets() {
    }

    public static List<String> forCurrentHost(List<String> configured) {
        List<String> targets = configured == null || configured.isEmpty() ? List.of() : configured;
        if (targets.isEmpty()) {
            return defaultForHost();
        }
        List<String> runnable = new ArrayList<>();
        for (String target : targets) {
            if (canRunOnHost(target)) {
                runnable.add(target);
            }
        }
        return runnable.isEmpty() ? defaultForHost() : runnable;
    }

    private static List<String> defaultForHost() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac")) {
            if (arch.contains("aarch64") || arch.contains("arm")) {
                return List.of("macM1");
            }
            return List.of("macX64");
        }
        if (os.contains("win")) {
            return List.of("winX64");
        }
        return List.of("linuxX64");
    }

    private static boolean canRunOnHost(String target) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        return switch (target) {
            case "linuxX64" -> os.contains("linux");
            case "winX64" -> os.contains("win");
            case "macM1" -> os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm"));
            case "macX64" -> os.contains("mac") && !arch.contains("aarch64") && !arch.contains("arm");
            default -> true;
        };
    }
}
