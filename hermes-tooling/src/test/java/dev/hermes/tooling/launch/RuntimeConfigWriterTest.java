package dev.hermes.tooling.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class RuntimeConfigWriterTest {

    @Test
    void write_producesReadablePropertiesFile(@TempDir Path dir) throws Exception {
        RuntimeConfigWriter.write(dir.toFile(), Map.of("hermes.log.minLevel", "WARN"));
        Properties loaded = new Properties();
        try (InputStream in = Files.newInputStream(dir.resolve("hermes-runtime.properties"))) {
            loaded.load(in);
        }
        assertEquals("WARN", loaded.getProperty("hermes.log.minLevel"));
    }
}
