package dev.hermes.tooling.doctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.tooling.doctor.HermesDoctorSupport.CheckResult;
import dev.hermes.tooling.doctor.HermesDoctorSupport.Status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class HermesDoctorSupportTest {

    @Test
    void checkHtmlCustomShaders_failsWhenHtmlEnabledAndPipelineDeclaresCustomGlsl(@TempDir Path project)
            throws IOException {
        writeHtmlEnabled(project);
        Files.createDirectories(project.resolve("game/src/main/resources/assets/render"));
        Files.writeString(
                project.resolve("game/hermes.json"),
                "{"
                        + "\"title\":\"Test\","
                        + "\"scene\":\"scenes/main.json\","
                        + "\"renderPipeline\":\"render/custom-pipeline.json\","
                        + "\"inputProfile\":\"input/profile.json\""
                        + "}",
                StandardCharsets.UTF_8);
        Files.writeString(
                project.resolve("game/src/main/resources/assets/render/custom-pipeline.json"),
                "{"
                        + "\"version\":1,"
                        + "\"shaders\":{"
                        + "\"water\":{"
                        + "\"vertex\":\"shaders/water.vert\","
                        + "\"fragment\":\"shaders/water.frag\""
                        + "}},"
                        + "\"passes\":[]"
                        + "}",
                StandardCharsets.UTF_8);

        CheckResult result = HermesDoctorSupport.checkHtmlCustomShaders(project);

        assertEquals(Status.FAIL, result.status());
        assertTrue(result.message().contains("water.frag"));
    }

    @Test
    void checkHtmlCustomShaders_okWhenCustomGlslOnDiskButPipelineUsesBuiltinOnly(@TempDir Path project)
            throws IOException {
        writeHtmlEnabled(project);
        Files.createDirectories(project.resolve("game/src/main/resources/assets/shaders"));
        Files.writeString(
                project.resolve("game/src/main/resources/assets/shaders/water.frag"),
                "// orphan on disk",
                StandardCharsets.UTF_8);
        Files.createDirectories(project.resolve("game/src/main/resources/assets/render"));
        Files.writeString(
                project.resolve("game/hermes.json"),
                "{"
                        + "\"title\":\"Test\","
                        + "\"scene\":\"scenes/main.json\","
                        + "\"renderPipeline\":\"render/pipeline.json\","
                        + "\"inputProfile\":\"input/profile.json\""
                        + "}",
                StandardCharsets.UTF_8);
        Files.writeString(
                project.resolve("game/src/main/resources/assets/render/pipeline.json"),
                "{"
                        + "\"version\":1,"
                        + "\"shaders\":{"
                        + "\"default/unlit\":{"
                        + "\"vertex\":\"shaders/default.vert\","
                        + "\"fragment\":\"shaders/default.frag\""
                        + "}},"
                        + "\"passes\":[]"
                        + "}",
                StandardCharsets.UTF_8);

        CheckResult result = HermesDoctorSupport.checkHtmlCustomShaders(project);

        assertEquals(Status.OK, result.status());
    }

    @Test
    void checkHtmlCustomShaders_okWhenHtmlDisabled(@TempDir Path project) throws IOException {
        Files.createDirectories(project.resolve("game/src/main/resources/assets/render"));
        Files.writeString(
                project.resolve("game/hermes.json"),
                "{"
                        + "\"title\":\"Test\","
                        + "\"scene\":\"scenes/main.json\","
                        + "\"renderPipeline\":\"render/custom-pipeline.json\","
                        + "\"inputProfile\":\"input/profile.json\""
                        + "}",
                StandardCharsets.UTF_8);
        Files.writeString(
                project.resolve("game/src/main/resources/assets/render/custom-pipeline.json"),
                "{"
                        + "\"version\":1,"
                        + "\"shaders\":{"
                        + "\"water\":{"
                        + "\"vertex\":\"shaders/water.vert\","
                        + "\"fragment\":\"shaders/water.frag\""
                        + "}},"
                        + "\"passes\":[]"
                        + "}",
                StandardCharsets.UTF_8);
        Files.writeString(
                project.resolve("settings.gradle"),
                "hermes { platforms { html { enabled = false } } }\n",
                StandardCharsets.UTF_8);

        CheckResult result = HermesDoctorSupport.checkHtmlCustomShaders(project);

        assertEquals(Status.OK, result.status());
    }

    @Test
    void runStandalone_usesCustomGameModuleFromSettings(@TempDir Path project) throws IOException {
        Files.writeString(
                project.resolve("settings.gradle"),
                "hermes {\n    gameModule = 'my-game'\n}\ninclude 'my-game'\n",
                StandardCharsets.UTF_8);
        Files.createDirectories(project.resolve("my-game"));
        Files.writeString(
                project.resolve("my-game/hermes.json"),
                "{\"title\":\"Test\",\"scene\":\"scenes/main.json\",\"renderPipeline\":\"render/pipeline.json\",\"inputProfile\":\"input/profile.json\"}",
                StandardCharsets.UTF_8);

        var results = HermesDoctorSupport.runStandalone(project);

        assertTrue(
                results.stream()
                        .anyMatch(
                                r ->
                                        "hermes.json".equals(r.name())
                                                && r.status() == Status.OK),
                "hermes.json check should pass for my-game module");
        assertTrue(
                results.stream().noneMatch(r -> "project-layout".equals(r.name())),
                "project-layout should not fail when my-game module exists");
    }

    @Test
    void checkHtmlCustomShaders_usesCustomGameModulePaths(@TempDir Path project) throws IOException {
        writeHtmlEnabled(project);
        Files.writeString(
                project.resolve("settings.gradle"),
                "hermes {\n    gameModule = 'my-game'\n    platforms { html { enabled = true } }\n}\n",
                StandardCharsets.UTF_8);
        Files.createDirectories(project.resolve("my-game/src/main/resources/assets/render"));
        Files.writeString(
                project.resolve("my-game/hermes.json"),
                "{"
                        + "\"title\":\"Test\","
                        + "\"scene\":\"scenes/main.json\","
                        + "\"renderPipeline\":\"render/custom-pipeline.json\","
                        + "\"inputProfile\":\"input/profile.json\""
                        + "}",
                StandardCharsets.UTF_8);
        Files.writeString(
                project.resolve("my-game/src/main/resources/assets/render/custom-pipeline.json"),
                "{"
                        + "\"version\":1,"
                        + "\"shaders\":{"
                        + "\"water\":{"
                        + "\"vertex\":\"shaders/water.vert\","
                        + "\"fragment\":\"shaders/water.frag\""
                        + "}},"
                        + "\"passes\":[]"
                        + "}",
                StandardCharsets.UTF_8);

        CheckResult result = HermesDoctorSupport.checkHtmlCustomShaders(project);

        assertEquals(Status.FAIL, result.status());
        assertTrue(result.message().contains("water.frag"));
    }

    @Test
    void checkHtmlCustomShaders_okWhenPipelineUsesDefaultUnlit(@TempDir Path project) throws IOException {
        writeHtmlEnabled(project);
        Files.createDirectories(project.resolve("game/src/main/resources/assets/render"));
        Files.writeString(
                project.resolve("game/hermes.json"),
                "{"
                        + "\"title\":\"Test\","
                        + "\"scene\":\"scenes/main.json\","
                        + "\"renderPipeline\":\"render/pipeline.json\","
                        + "\"inputProfile\":\"input/profile.json\""
                        + "}",
                StandardCharsets.UTF_8);
        Files.writeString(
                project.resolve("game/src/main/resources/assets/render/pipeline.json"),
                "{"
                        + "\"version\":1,"
                        + "\"shaders\":{"
                        + "\"default/unlit\":{"
                        + "\"vertex\":\"shaders/default.vert\","
                        + "\"fragment\":\"shaders/default-unlit.frag\""
                        + "}},"
                        + "\"passes\":[]"
                        + "}",
                StandardCharsets.UTF_8);

        CheckResult result = HermesDoctorSupport.checkHtmlCustomShaders(project);

        assertEquals(Status.OK, result.status());
    }

    @Test
    void findCustomShaderFiles_ignoresDefaultShaders(@TempDir Path shadersDir) throws IOException {
        Files.createDirectories(shadersDir);
        Files.writeString(shadersDir.resolve("default.vert"), "", StandardCharsets.UTF_8);
        Files.writeString(shadersDir.resolve("default.frag"), "", StandardCharsets.UTF_8);
        Files.writeString(shadersDir.resolve("default-unlit.frag"), "", StandardCharsets.UTF_8);
        Files.writeString(shadersDir.resolve("toon.frag"), "", StandardCharsets.UTF_8);

        var custom = HermesDoctorSupport.findCustomShaderFiles(shadersDir);

        assertEquals(1, custom.size());
        assertTrue(custom.get(0).endsWith("toon.frag"));
    }

    private static void writeHtmlEnabled(Path project) throws IOException {
        Files.writeString(
                project.resolve("settings.gradle"),
                "hermes { platforms { html { enabled = true } } }\n",
                StandardCharsets.UTF_8);
    }
}
