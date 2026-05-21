package dev.hermes.core.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SceneRegistryTest {

  private HermesEngineImpl engine;

  @BeforeAll
  static void initGdx() {
    TestGdx.initClasspathFiles();
  }

  @BeforeEach
  void setUp() {
    engine = new HermesEngineImpl();
  }

  @Test
  void registerIdAndAssetPathCreatesAssetSceneSource() {
    engine.scenes().registry().register("main", "scenes/main.json");

    engine.scenes().request(SceneChangeRequest.goTo("main"));
    engine.scenes().processPending();

    Entity logo = engine.scenes().activeWorld().findByName("logo");
    assertNotNull(logo);
    assertEquals(140f, engine.scenes().activeWorld().getComponent(logo.id(), Transform.class).x());
    assertEquals(210f, engine.scenes().activeWorld().getComponent(logo.id(), Transform.class).y());
    assertEquals(
        "hermes-logo.png",
        engine.scenes().activeWorld().getComponent(logo.id(), Sprite.class).texture());
  }

  @Test
  void registerSceneDefinitionRejectsDuplicateIds() {
    SceneDefinition definition =
        SceneDefinition.builder("main")
            .source(new AssetSceneSource("scenes/main.json"))
            .build();

    engine.scenes().registry().register(definition);

    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                engine
                    .scenes()
                    .registry()
                    .register(
                        SceneDefinition.builder("main")
                            .source(new AssetSceneSource("scenes/other.json"))
                            .build()));

    assertTrue(error.getMessage().contains("main"));
    assertTrue(error.getMessage().contains("already registered"));
  }

  @Test
  void registerIdAndPathRejectsDuplicateIds() {
    engine.scenes().registry().register("main", "scenes/main.json");

    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () -> engine.scenes().registry().register("main", "scenes/other.json"));

    assertTrue(error.getMessage().contains("main"));
    assertTrue(error.getMessage().contains("already registered"));
  }
}
