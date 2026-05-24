package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.gradle.export.HermesDesktopExportTargets;

import java.util.List;

import org.junit.jupiter.api.Test;

class HermesDesktopExportTargetsTest {

    @Test
    void forCurrentHost_filtersNonRunnableTargets() {
        String os = System.getProperty("os.name", "").toLowerCase();
        List<String> result =
                HermesDesktopExportTargets.forCurrentHost(
                        List.of("linuxX64", "macM1", "macX64", "winX64"));
        if (os.contains("mac")) {
            String arch = System.getProperty("os.arch", "").toLowerCase();
            if (arch.contains("aarch64") || arch.contains("arm")) {
                assertEquals(List.of("macM1"), result);
            } else {
                assertEquals(List.of("macX64"), result);
            }
        } else if (os.contains("win")) {
            assertEquals(List.of("winX64"), result);
        } else {
            assertEquals(List.of("linuxX64"), result);
        }
    }
}
