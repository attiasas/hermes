package dev.hermes.tooling.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.tooling.doctor.HermesDoctorSupport.CheckResult;
import dev.hermes.tooling.doctor.HermesDoctorSupport.Status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class HermesDoctorResourceBundleTest {

    @Test
    void failsWhenHtmlEnabledAndBundleReferencesGlb(@TempDir Path project) throws Exception {
        writeMinimalHermesProject(project, true);
        writeBundle(project, "boot", List.of("models/hero.glb"));
        CheckResult result = HermesDoctorSupport.checkResourceBundles(project);
        assertEquals(Status.FAIL, result.status());
        assertTrue(result.message().contains(".glb"));
    }

    @Test
    void warnsWhenHtmlEnabledAndBundleReferencesSound(@TempDir Path project) throws Exception {
        writeMinimalHermesProject(project, true);
        writeBundleWithSound(project, "boot", "sfx/click.wav");
        CheckResult result = HermesDoctorSupport.checkResourceBundles(project);
        assertEquals(Status.WARN, result.status());
    }

    @Test
    void okWhenHtmlDisabled(@TempDir Path project) throws Exception {
        writeMinimalHermesProject(project, false);
        writeBundle(project, "boot", List.of("models/hero.glb"));
        writeBundleWithSound(project, "audio", "sfx/click.wav");
        CheckResult result = HermesDoctorSupport.checkResourceBundles(project);
        assertEquals(Status.OK, result.status());
    }

    private static void writeMinimalHermesProject(Path project, boolean htmlEnabled) throws IOException {
        Files.writeString(
                project.resolve("settings.gradle"),
                htmlEnabled
                        ? "hermes { platforms { html { enabled = true } } }\n"
                        : "hermes { platforms { html { enabled = false } } }\n",
                StandardCharsets.UTF_8);
        Files.createDirectories(project.resolve("game"));
    }

    private static void writeBundle(Path project, String id, List<String> paths) throws IOException {
        Path bundleDir = project.resolve("game/src/main/resources/assets/resources/bundles");
        Files.createDirectories(bundleDir);
        StringBuilder resources = new StringBuilder();
        for (int i = 0; i < paths.size(); i++) {
            if (i > 0) {
                resources.append(",\n");
            }
            resources.append("    { \"ref\": \"").append(paths.get(i)).append("\", \"kind\": \"model\" }");
        }
        Files.writeString(
                bundleDir.resolve(id + ".json"),
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"id\": \""
                        + id
                        + "\",\n"
                        + "  \"resources\": [\n"
                        + resources
                        + "\n  ]\n"
                        + "}\n",
                StandardCharsets.UTF_8);
    }

    private static void writeBundleWithSound(Path project, String id, String soundPath) throws IOException {
        Path bundleDir = project.resolve("game/src/main/resources/assets/resources/bundles");
        Files.createDirectories(bundleDir);
        Files.writeString(
                bundleDir.resolve(id + ".json"),
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"id\": \""
                        + id
                        + "\",\n"
                        + "  \"resources\": [\n"
                        + "    { \"ref\": \""
                        + soundPath
                        + "\", \"kind\": \"sound\" }\n"
                        + "  ]\n"
                        + "}\n",
                StandardCharsets.UTF_8);
    }
}
