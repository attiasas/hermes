package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import org.junit.jupiter.api.Test;

final class ComponentRefResolverTest {

    private final JsonReader reader = new JsonReader();

    @Test
    void resolve_replacesRefWithLiteral_fromMergedTransform() {
        JsonValue merged =
                reader.parse(
                        "{\"Transform\":{\"x\":7,\"y\":2},"
                                + "\"SpinMarker\":{\"centerX\":{\"$ref\":\"Transform.x\"},"
                                + "\"centerY\":{\"$ref\":\"Transform.y\"}}}");
        ComponentRefResolver.resolve("test", "e1", merged);
        assertEquals(7f, merged.get("SpinMarker").getFloat("centerX"), 0.001f);
        assertEquals(2f, merged.get("SpinMarker").getFloat("centerY"), 0.001f);
        assertFalse(merged.get("SpinMarker").get("centerX").isObject());
    }

    @Test
    void resolve_missingTransform_throws() {
        JsonValue merged = reader.parse("{\"SpinMarker\":{\"centerX\":{\"$ref\":\"Transform.x\"}}}");
        assertThrows(
                SceneParseException.class, () -> ComponentRefResolver.resolve("test", "e1", merged));
    }
}
