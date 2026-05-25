package dev.hermes.tooling.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GradlePropertySupportTest {

    @Test
    void firstNonBlank_skipsBlankValues() {
        assertEquals("a", GradlePropertySupport.firstNonBlank(null, "", "  ", "a", "b"));
        assertNull(GradlePropertySupport.firstNonBlank(null, "", "  "));
    }

    @Test
    void readProperty_readsFromGradleProperties(@TempDir Path projectDir) throws Exception {
        Files.writeString(
                projectDir.resolve("gradle.properties"),
                "hermes.home=/opt/hermes\nother=value\n",
                StandardCharsets.UTF_8);

        assertEquals("/opt/hermes", GradlePropertySupport.readProperty(projectDir, "hermes.home"));
        assertNull(GradlePropertySupport.readProperty(projectDir, "missing"));
        assertNull(GradlePropertySupport.readProperty(projectDir.resolve("none"), "hermes.home"));
    }
}
