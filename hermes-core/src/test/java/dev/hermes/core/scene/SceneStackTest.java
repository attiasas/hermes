package dev.hermes.core.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.scene.SceneContext;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneLifecycle;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.api.scene.SceneSource;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneRegistryImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SceneStackTest {

  private ComponentRegistryImpl componentRegistry;
  private SceneRegistryImpl sceneRegistry;
  private SceneStack stack;

  @BeforeEach
  void setUp() {
    componentRegistry = new ComponentRegistryImpl();
    sceneRegistry = new SceneRegistryImpl(componentRegistry);
    stack = new SceneStack(sceneRegistry);
    registerScene("a", "marker-a");
    registerScene("b", "marker-b");
  }

  @Test
  void goToReplacesStackAndOnlyActiveSceneHasEntities() {
    stack.goTo("a");
    assertEquals(1, stack.depth());
    assertNotNull(stack.active().world().findByName("marker-a"));

    stack.goTo("b");
    assertEquals(1, stack.depth());
    assertEquals("b", stack.active().id());
    assertNull(stack.active().world().findByName("marker-a"));
    assertNotNull(stack.active().world().findByName("marker-b"));
  }

  @Test
  void pushPausesCurrentSceneAndActivatesNewOne() {
    stack.goTo("a");
    SceneInstance sceneA = stack.active();

    stack.push("b");

    assertEquals(2, stack.depth());
    assertTrue(sceneA.paused());
    assertFalse(stack.active().paused());
    assertEquals("b", stack.active().id());
    assertNotNull(sceneA.world().findByName("marker-a"));
    assertNotNull(stack.active().world().findByName("marker-b"));
  }

  @Test
  void popResumesPreviousSceneAndDisposesTopWorld() {
    stack.goTo("a");
    stack.push("b");
    SceneInstance sceneB = stack.active();

    stack.pop();

    assertEquals(1, stack.depth());
    assertEquals("a", stack.active().id());
    assertFalse(stack.active().paused());
    assertNotNull(stack.active().world().findByName("marker-a"));
    assertEquals(0, sceneB.world().entityCount());
  }

  @Test
  void popOnEmptyThrows() {
    IllegalStateException error = assertThrows(IllegalStateException.class, stack::pop);
    assertTrue(error.getMessage().contains("empty"));
  }

  @Test
  void lifecycleHooksFireOnTransitions() {
    RecordingLifecycle lifecycleA = new RecordingLifecycle();
    RecordingLifecycle lifecycleB = new RecordingLifecycle();
    sceneRegistry.register(
        SceneDefinition.builder("la").source(markerSource("la-marker")).lifecycle(lifecycleA).build());
    sceneRegistry.register(
        SceneDefinition.builder("lb").source(markerSource("lb-marker")).lifecycle(lifecycleB).build());

    stack.goTo("la");
    assertEquals(List.of("enter:la"), lifecycleA.events);

    stack.push("lb");
    assertEquals(List.of("enter:la", "pause:la"), lifecycleA.events);
    assertEquals(List.of("enter:lb"), lifecycleB.events);

    stack.pop();
    assertEquals(List.of("enter:la", "pause:la", "resume:la"), lifecycleA.events);
    assertEquals(List.of("enter:lb", "exit:lb"), lifecycleB.events);

    stack.goTo("lb");
    assertEquals(List.of("enter:la", "pause:la", "resume:la", "exit:la"), lifecycleA.events);
    assertEquals(List.of("enter:lb", "exit:lb", "enter:lb"), lifecycleB.events);
  }

  private void registerScene(String id, String markerName) {
    sceneRegistry.register(SceneDefinition.builder(id).source(markerSource(markerName)).build());
  }

  private static SceneSource markerSource(String markerName) {
    return (SceneLoadContext ctx) -> ctx.world().createEntity(markerName);
  }

  private static final class RecordingLifecycle implements SceneLifecycle {

    private final List<String> events = new ArrayList<>();

    @Override
    public void onEnter(SceneContext ctx) {
      events.add("enter:" + ctx.sceneId());
    }

    @Override
    public void onExit(SceneContext ctx) {
      events.add("exit:" + ctx.sceneId());
    }

    @Override
    public void onPause(SceneContext ctx) {
      events.add("pause:" + ctx.sceneId());
    }

    @Override
    public void onResume(SceneContext ctx) {
      events.add("resume:" + ctx.sceneId());
    }
  }
}
