package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import java.util.Optional;
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
            + "        \"Sprite\": { \"texture\": \"hermes-logo.png\" },\n"
            + "        \"Material\": { \"shader\": \"default/unlit\" }\n"
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
    assertEquals("hermes-logo.png", world.getComponent(logo.id(), Sprite.class).texture());
  }

  @Test
  void loadsTransform3dFields() {
    String json =
        "{\n"
            + "  \"entities\": [\n"
            + "    {\n"
            + "      \"id\": \"cube\",\n"
            + "      \"components\": {\n"
            + "        \"Transform\": {\n"
            + "          \"x\": 1,\n"
            + "          \"y\": 2,\n"
            + "          \"z\": 3,\n"
            + "          \"rotationX\": 10,\n"
            + "          \"rotationY\": 20,\n"
            + "          \"rotationZ\": 30,\n"
            + "          \"scaleX\": 2,\n"
            + "          \"scaleY\": 2,\n"
            + "          \"scaleZ\": 2\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}\n";

    SceneLoader.loadFromString("scenes/test.json", json, world, registry);

    Transform transform = world.getComponent(world.findByName("cube").id(), Transform.class);
    assertEquals(1f, transform.x());
    assertEquals(2f, transform.y());
    assertEquals(3f, transform.z());
    assertEquals(10f, transform.rotationX());
    assertEquals(20f, transform.rotationY());
    assertEquals(30f, transform.rotationZ());
    assertEquals(2f, transform.scaleX());
    assertEquals(2f, transform.scaleY());
    assertEquals(2f, transform.scaleZ());
  }

  @Test
  void nonObjectEntityElementThrowsSceneParseException() {
    String json = "{\n  \"entities\": [ \"not-an-object\" ]\n}\n";

    SceneParseException error =
        assertThrows(
            SceneParseException.class,
            () -> SceneLoader.loadFromString("scenes/bad.json", json, world, registry));

    assertTrue(error.getMessage().contains("entities[0]"));
    assertTrue(error.getMessage().contains("must be an object"));
  }

  @Test
  void loadsEntityKindFromJson() {
    String json =
        "{\n"
            + "  \"entities\": [\n"
            + "    {\n"
            + "      \"id\": \"player\",\n"
            + "      \"kind\": \"character\",\n"
            + "      \"components\": {\n"
            + "        \"Transform\": { \"x\": 0, \"y\": 0 }\n"
            + "      }\n"
            + "    },\n"
            + "    {\n"
            + "      \"id\": \"logo\",\n"
            + "      \"components\": {\n"
            + "        \"Sprite\": { \"texture\": \"logo.png\" },\n"
            + "        \"Material\": { \"shader\": \"default/unlit\" }\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}\n";

    SceneLoader.loadFromString("scenes/main.json", json, world, registry);

    Entity player = world.findByName("player");
    assertNotNull(player);
    assertEquals(EntityKind.of("character"), player.kind());

    Entity logo = world.findByName("logo");
    assertNotNull(logo);
    assertEquals(EntityKind.UNSET, logo.kind());
  }

  @Test
  void loadsOptionalRenderPipeline() {
    String json =
        "{\n"
            + "  \"renderPipeline\": \"render/ui-overlay.json\",\n"
            + "  \"entities\": []\n"
            + "}\n";

    Optional<String> override =
        SceneLoader.loadFromString("scenes/overlay.json", json, world, registry);

    assertEquals(Optional.of("render/ui-overlay.json"), override);
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
