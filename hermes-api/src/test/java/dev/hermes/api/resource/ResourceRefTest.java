package dev.hermes.api.resource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceRefTest {

    @Test
    void normalizesPathAndRejectsBlank() {
        ResourceRef ref = ResourceRef.of("textures/logo.png");
        assertEquals("textures/logo.png", ref.path());
        assertFalse(ref.alias());
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.of("  "));
    }

    @Test
    void aliasPrefix() {
        ResourceRef ref = ResourceRef.of("@logo");
        assertTrue(ref.alias());
        assertEquals("@logo", ref.raw());
    }
}
