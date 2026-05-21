package dev.hermes.api.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class MutableComponentDataTest {

  @Test
  void storesAndReadsValues() {
    MutableComponentData data = new MutableComponentData();
    data.putFloat("x", 10f);
    assertEquals(10f, data.getFloat("x", 0f));
  }
}
