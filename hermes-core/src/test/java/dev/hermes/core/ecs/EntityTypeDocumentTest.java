package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class EntityTypeDocumentTest {

    @Test
    void parse_validTypeJson() {
        String json = "{\"version\":1,\"components\":{\"Transform\":{\"z\":0}}}";
        EntityTypeDocument doc = EntityTypeDocument.parse("entities/spin-cube/type.json", json);
        assertEquals(1, doc.version());
        assertTrue(doc.componentsJson().has("Transform"));
    }

    @Test
    void parse_rejectsWrongVersion() {
        assertThrows(
                SceneParseException.class,
                () -> EntityTypeDocument.parse("entities/x/type.json", "{\"version\":2}"));
    }
}
