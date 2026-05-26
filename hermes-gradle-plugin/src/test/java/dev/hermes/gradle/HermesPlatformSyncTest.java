package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.gradle.platform.HermesPlatformSync;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesPlatformSyncTest {

    @Test
    void syncIfNeeded_copiesFullLauncherIncludingBuildGradle(@TempDir Path tempDir) throws Exception {
        Path root = tempDir.resolve("project");
        HermesPlatformSync.syncIfNeeded(root.toFile(), "hermes-launcher-android", "0.1.0-SNAPSHOT", null);

        Path buildGradle = root.resolve(".hermes/platforms/hermes-launcher-android/build.gradle");
        assertTrue(Files.isRegularFile(buildGradle));
        String content = Files.readString(buildGradle, StandardCharsets.UTF_8);
        assertTrue(content.contains("dev.hermes.launcher.android"));
        assertFalse(content.contains("{{hermesCoreDependency}}"));
    }

    @Test
    void syncIfNeeded_refreshesSourcesWhenEngineVersionChanges(@TempDir Path tempDir) throws Exception {
        Path root = tempDir.resolve("game");
        Path home = tempDir.resolve("hermes-home");
        Files.createDirectories(home.resolve("hermes-api"));
        Files.createDirectories(home.resolve("hermes-core"));
        Path desktopHome = home.resolve("hermes-launcher-desktop/src/main/java/dev/hermes/launcher/desktop");
        Files.createDirectories(desktopHome);
        Files.writeString(
                desktopHome.resolve("Lwjgl3Launcher.java"),
                "package dev.hermes.launcher.desktop;\npublic final class Lwjgl3Launcher {}\n",
                StandardCharsets.UTF_8);
        Files.createDirectories(home.resolve("hermes-launcher-desktop"));
        Files.writeString(home.resolve("hermes-launcher-desktop/build.gradle"), "// home stub\n", StandardCharsets.UTF_8);

        Path syncedDesktop =
                root.resolve(".hermes/platforms/hermes-launcher-desktop/src/main/java/dev/hermes/launcher/desktop");
        Files.createDirectories(syncedDesktop);
        Files.writeString(syncedDesktop.resolve("StaleLauncher.java"), "stale\n", StandardCharsets.UTF_8);
        Files.writeString(syncedDesktop.resolve("Lwjgl3Launcher.java"), "stale\n", StandardCharsets.UTF_8);
        Files.createDirectories(root.resolve(".hermes/platforms/hermes-launcher-desktop"));
        Files.writeString(
                root.resolve(".hermes/platforms/hermes-launcher-desktop/build.gradle"),
                "old\n",
                StandardCharsets.UTF_8);
        Files.createDirectories(root.resolve(".hermes"));
        Files.writeString(root.resolve(".hermes/version"), "0.1.0-SNAPSHOT\n", StandardCharsets.UTF_8);

        HermesPlatformSync.syncIfNeeded(
                root.toFile(), "hermes-launcher-desktop", "0.2.0-SNAPSHOT", home.toFile());

        assertFalse(Files.exists(syncedDesktop.resolve("StaleLauncher.java")));
        assertTrue(
                Files.readString(syncedDesktop.resolve("Lwjgl3Launcher.java"), StandardCharsets.UTF_8)
                        .contains("public final class Lwjgl3Launcher"));
    }
}
