package dev.hermes.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class HdpJsonTest {

  private final Gson gson = new Gson();

  @Test
  void roundTripsWorldSnapshot() {
    WorldSnapshot snapshot =
        new WorldSnapshot(
            42L,
            "scenes/main.json",
            List.of(
                new EntitySnapshot(
                    "1",
                    "logo",
                    List.of(
                        new ComponentSnapshot(
                            "Transform",
                            Map.of("x", 140.0, "y", 210.0),
                            List.of(
                                new FieldSnapshot("x", FieldKind.FLOAT, true, 140.0),
                                new FieldSnapshot("y", FieldKind.FLOAT, true, 210.0)))))));
    String json = gson.toJson(HdpMessage.worldSnapshot(snapshot));
    HdpMessage parsed = gson.fromJson(json, HdpMessage.class);
    assertEquals("worldSnapshot", parsed.type());
    assertEquals(140.0, parsed.worldSnapshot().entities().get(0).components().get(0).fields().get(0).value());
  }
}
