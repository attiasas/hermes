package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SceneParserTest {

  private WorldImpl world;
  private ComponentRegistryImpl registry;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    registry = new ComponentRegistryImpl();
    BuiltinComponents.register(registry);
  }

  @Test
  void loadsEntitiesAndComponents() {
    String json =
        "{\n"
            + "  \"entities\": [\n"
            + "    {\n"
            + "      \"id\": \"logo\",\n"
            + "      \"components\": {\n"
            + "        \"Transform\": { \"x\": 140, \"y\": 210 },\n"
            + "        \"Sprite\": { \"texture\": \"libgdx.png\" }\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}\n";

    SceneLoader.loadFromString("scenes/main.json", json, world, registry);

    assertEquals(1, world.entityCount());
    Entity logo = world.findByName("logo");
    assertNotNull(logo);
    assertEquals(140f, world.getComponent(logo.id(), Transform.class).x());
    assertEquals(210f, world.getComponent(logo.id(), Transform.class).y());
    assertEquals("libgdx.png", world.getComponent(logo.id(), Sprite.class).texture());
  }

  @Test
  void unknownComponentTypeFailsWithActionableMessage() {
    String json =
        "{\n"
            + "  \"entities\": [\n"
            + "    {\n"
            + "      \"id\": \"player\",\n"
            + "      \"components\": {\n"
            + "        \"Foo\": { \"x\": 1 }\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}\n";

    SceneLoadException error =
        assertThrows(
            SceneLoadException.class,
            () -> SceneLoader.loadFromString("scenes/main.json", json, world, registry));

    assertTrue(error.getMessage().contains("unknown component 'Foo'"));
    assertTrue(error.getMessage().contains("entity 'player'"));
  }
}
