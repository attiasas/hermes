package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.Transform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class EntityStoreImplTest {

    private EntityStoreImpl world;

    @BeforeEach
    void setUp() {
        world = new EntityStoreImpl();
    }

    @Test
    void createAndAddComponents() {
        Entity entity = world.create("player");
        world.addComponent(entity.id(), new Transform(10f, 20f));
        world.addComponent(entity.id(), Drawables.singleSprite("logo.png"));

        assertEquals(1, world.entityCount());
        assertEquals("player", entity.name());
        assertNotNull(world.findByName("player"));
        assertEquals(10f, world.getComponent(entity.id(), Transform.class).x());
        assertEquals("logo.png", world.getComponent(entity.id(), Drawables.class).parts().get(0).texture());
        assertTrue(world.hasComponent(entity.id(), Transform.class));
    }

    @Test
    void removeEntityAndComponent() {
        Entity entity = world.create("temp");
        world.addComponent(entity.id(), new Transform());
        world.removeComponent(entity.id(), Transform.class);
        assertFalse(world.hasComponent(entity.id(), Transform.class));

        world.removeEntity(entity.id());
        assertEquals(0, world.entityCount());
        assertNull(world.findByName("temp"));
    }

    @Test
    void create_duplicateNameThrows() {
        world.create("player");
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> world.create("player"));
        assertTrue(error.getMessage().contains("Duplicate entity name"));
    }

    @Test
    void entitiesWithFiltersByComponentType() {
        Entity a = world.create("a");
        Entity b = world.create("b");
        world.addComponent(a.id(), Drawables.singleSprite("a.png"));
        world.addComponent(b.id(), new Transform());

        assertEquals(1, world.entitiesWith(Drawables.class).size());
        assertEquals(1, world.entitiesWith(Transform.class).size());
    }

    @Test
    void clearRemovesAllEntitiesAndComponents() {
        Entity a = world.create("a", EntityKind.of("character"));
        world.addComponent(a.id(), new Transform());
        Entity b = world.create("b");
        world.addComponent(b.id(), Drawables.singleSprite("x.png"));

        world.clear();

        assertEquals(0, world.entityCount());
        assertNull(world.findByName("a"));
        assertNull(world.findByName("b"));
        assertFalse(world.hasComponent(a.id(), Transform.class));

        Entity c = world.create("c");
        assertEquals(1, world.entityCount());
    }

    @Test
    void createWithoutKindUsesUnset() {
        Entity entity = world.create("generic");
        assertEquals(EntityKind.UNSET, entity.kind());
    }

    @Test
    void entitiesWithKindFiltersByKind() {
        world.create("player", EntityKind.of("character"));
        world.create("npc", EntityKind.of("character"));
        world.create("wall", EntityKind.of("static"));
        world.create("generic");

        assertEquals(2, world.entitiesWithKind(EntityKind.of("character")).size());
        assertEquals(1, world.entitiesWithKind(EntityKind.of("static")).size());
        assertEquals(1, world.entitiesWithKind(EntityKind.UNSET).size());
    }
}
