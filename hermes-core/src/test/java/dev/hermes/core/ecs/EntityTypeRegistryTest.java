package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class EntityTypeRegistryTest {

    @Test
    void scan_registersDirectoryNameAsKind() {
        EntityTypeRegistryImpl registry = new EntityTypeRegistryImpl();
        registry.scanTestAssets("entities/spin-cube/type.json");
        assertTrue(registry.has("spin-cube"));
        assertEquals(
                "default/unlit",
                registry.require("spin-cube").componentsJson().get("Material").getString("shader"));
    }

    @Test
    void require_throwsWhenMissing() {
        EntityTypeRegistryImpl registry = new EntityTypeRegistryImpl();
        assertThrows(IllegalArgumentException.class, () -> registry.require("missing"));
    }
}
