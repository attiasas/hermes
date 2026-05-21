package dev.hermes.core.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.debug.WorldSnapshot;
import org.junit.jupiter.api.Test;

final class WorldSnapshotBuilderTest {

  @Test
  void buildsEntityWithTransform() {
    HermesEngineImpl engine = new HermesEngineImpl();
    Entity e = engine.world().createEntity("logo");
    engine.world().addComponent(e.id(), new Transform(1f, 2f));
    WorldSnapshotBuilder builder = new WorldSnapshotBuilder(engine.registryImpl());
    WorldSnapshot snap = builder.build(engine.world(), "scenes/main.json", 1L);
    assertEquals(1, snap.entities().size());
    assertEquals("logo", snap.entities().get(0).name());
  }
}
