package dev.hermes.core.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.WorldImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class AssetSceneSourceTest {

  private World world;
  private ComponentRegistry registry;

  @BeforeAll
  static void initGdx() {
    TestGdx.initClasspathFiles();
  }

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    registry = new HermesEngineImpl().registry();
  }

  @Test
  void populateCreatesEntitiesFromAsset() {
    AssetSceneSource source = new AssetSceneSource("scenes/main.json");
    source.populate(sceneLoadContext(world, registry));

    assertEquals(1, world.entityCount());
    Entity logo = world.findByName("logo");
    assertNotNull(logo);
    assertEquals(140f, world.getComponent(logo.id(), Transform.class).x());
    assertEquals(210f, world.getComponent(logo.id(), Transform.class).y());
    assertEquals("hermes-logo.png", world.getComponent(logo.id(), Sprite.class).texture());
  }

  private static SceneLoadContext sceneLoadContext(World world, ComponentRegistry registry) {
    return new SceneLoadContext() {
      @Override
      public World world() {
        return world;
      }

      @Override
      public ComponentRegistry registry() {
        return registry;
      }
    };
  }
}
