package dev.hermes.cli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.cli.HermesCli;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class DoctorCommandTest {

    @Test
    void doctor_minimalLayoutExitsZero(@TempDir Path dir) throws Exception {
        Files.createDirectories(dir.resolve("game"));
        Files.writeString(
                dir.resolve("game/hermes.json"),
                "{\n"
                        + "  \"title\": \"Test\",\n"
                        + "  \"scene\": \"scenes/main.json\",\n"
                        + "  \"renderPipeline\": \"render/pipeline.json\"\n"
                        + "}\n",
                java.nio.charset.StandardCharsets.UTF_8);

        int exit = new CommandLine(new HermesCli()).execute("doctor", dir.toString(), "--no-gradle");

        assertEquals(0, exit);
    }

    @Test
    void doctor_missingGameModuleFails(@TempDir Path dir) {
        int exit = new CommandLine(new HermesCli()).execute("doctor", dir.toString(), "--no-gradle");

        assertEquals(1, exit);
    }
}
