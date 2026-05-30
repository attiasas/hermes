package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import org.junit.jupiter.api.Test;

final class ComponentMergeTest {

    private final JsonReader reader = new JsonReader();

    @Test
    void merge_instanceOverridesTemplateField_keepsOtherTemplateFields() {
        JsonValue template = reader.parse("{\"Transform\":{\"x\":0,\"y\":1,\"z\":0}}");
        JsonValue instance = reader.parse("{\"Transform\":{\"x\":9}}");
        JsonValue merged = ComponentMerge.merge(template, instance);
        assertEquals(9f, merged.get("Transform").getFloat("x"), 0.001f);
        assertEquals(1f, merged.get("Transform").getFloat("y"), 0.001f);
    }
}
