package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class WorldImplTest {

  private WorldImpl world;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
  }

  @Test
  void createEntityAndAddComponents() {
    Entity entity = world.createEntity("player");
    world.addComponent(entity.id(), new Transform(10f, 20f));
    world.addComponent(entity.id(), new Sprite("logo.png"));

    assertEquals(1, world.entityCount());
    assertEquals("player", entity.name());
    assertNotNull(world.findByName("player"));
    assertEquals(10f, world.getComponent(entity.id(), Transform.class).x());
    assertEquals("logo.png", world.getComponent(entity.id(), Sprite.class).texture());
    assertTrue(world.hasComponent(entity.id(), Transform.class));
  }

  @Test
  void removeEntityAndComponent() {
    Entity entity = world.createEntity("temp");
    world.addComponent(entity.id(), new Transform());
    world.removeComponent(entity.id(), Transform.class);
    assertFalse(world.hasComponent(entity.id(), Transform.class));

    world.removeEntity(entity.id());
    assertEquals(0, world.entityCount());
    assertNull(world.findByName("temp"));
  }

  @Test
  void createEntity_duplicateNameThrows() {
    world.createEntity("player");
    IllegalArgumentException error =
        assertThrows(IllegalArgumentException.class, () -> world.createEntity("player"));
    assertTrue(error.getMessage().contains("Duplicate entity name"));
  }

  @Test
  void entitiesWithFiltersByComponentType() {
    Entity a = world.createEntity("a");
    Entity b = world.createEntity("b");
    world.addComponent(a.id(), new Sprite("a.png"));
    world.addComponent(b.id(), new Transform());

    assertEquals(1, world.entitiesWith(Sprite.class).size());
    assertEquals(1, world.entitiesWith(Transform.class).size());
  }

  @Test
  void clearRemovesAllEntitiesAndComponents() {
    Entity a = world.createEntity("a", EntityKind.of("character"));
    world.addComponent(a.id(), new Transform());
    Entity b = world.createEntity("b");
    world.addComponent(b.id(), new Sprite("x.png"));

    world.clear();

    assertEquals(0, world.entityCount());
    assertNull(world.findByName("a"));
    assertNull(world.findByName("b"));
    assertFalse(world.hasComponent(a.id(), Transform.class));

    Entity c = world.createEntity("c");
    assertEquals(1, world.entityCount());
  }

  @Test
  void createEntityWithoutKindUsesUnset() {
    Entity entity = world.createEntity("generic");
    assertEquals(EntityKind.UNSET, entity.kind());
  }

  @Test
  void entitiesWithKindFiltersByKind() {
    world.createEntity("player", EntityKind.of("character"));
    world.createEntity("npc", EntityKind.of("character"));
    world.createEntity("wall", EntityKind.of("static"));
    world.createEntity("generic");

    assertEquals(2, world.entitiesWithKind(EntityKind.of("character")).size());
    assertEquals(1, world.entitiesWithKind(EntityKind.of("static")).size());
    assertEquals(1, world.entitiesWithKind(EntityKind.UNSET).size());
  }
}
