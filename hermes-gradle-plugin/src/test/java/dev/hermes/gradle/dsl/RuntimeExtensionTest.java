package dev.hermes.gradle.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class RuntimeExtensionTest {

    @Test
    void put_storesCustomKeys() {
        RuntimeExtension runtime = new RuntimeExtension();
        runtime.put("seed", "42");
        assertEquals("42", runtime.asMap().get("seed"));
    }
}
