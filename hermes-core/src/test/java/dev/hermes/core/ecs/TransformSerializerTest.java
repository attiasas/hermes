package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.ecs.MutableComponentData;
import dev.hermes.api.ecs.Transform;
import org.junit.jupiter.api.Test;

final class TransformSerializerTest {

  @Test
  void serializesPosition() {
    Transform t = new Transform(3f, 4f);
    MutableComponentData data = new MutableComponentData();
    BuiltinComponentSerializers.transform().serialize(t, data);
    assertEquals(3f, data.getFloat("x", 0f));
    assertEquals(4f, data.getFloat("y", 0f));
  }

  @Test
  void applyFieldUpdatesX() {
    Transform t = new Transform(0f, 0f);
    BuiltinComponentSerializers.transform().applyField(t, "x", 99.0);
    assertEquals(99f, t.x());
  }
}
