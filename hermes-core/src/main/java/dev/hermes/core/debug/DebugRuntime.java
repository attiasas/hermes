package dev.hermes.core.debug;

import com.badlogic.gdx.Gdx;
import dev.hermes.api.Component;
import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.World;
import dev.hermes.core.HermesLauncherSupport;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.debug.StatsFrame;
import dev.hermes.debug.WorldSnapshot;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/** Debug session state shared by the overlay and HDP server. */
public final class DebugRuntime {

  private final HermesEngineImpl engine;
  private final WorldSnapshotBuilder snapshotBuilder;
  private final Supplier<Boolean> debugEnabled;
  private final AtomicBoolean paused = new AtomicBoolean(false);

  public DebugRuntime(
      HermesEngineImpl engine,
      WorldSnapshotBuilder snapshotBuilder,
      Supplier<Boolean> debugEnabled) {
    this.engine = engine;
    this.snapshotBuilder = snapshotBuilder;
    this.debugEnabled = debugEnabled;
  }

  public boolean isDebugEnabled() {
    return debugEnabled.get();
  }

  public boolean isPaused() {
    return paused.get();
  }

  public void setPaused(boolean value) {
    paused.set(value);
  }

  public HermesEngineImpl engine() {
    return engine;
  }

  public ComponentRegistryImpl registry() {
    return engine == null ? null : engine.registryImpl();
  }

  public WorldSnapshot buildWorldSnapshot(long frame) {
    if (snapshotBuilder == null || engine == null) {
      return new WorldSnapshot(frame, scenePath(), Collections.emptyList());
    }
    return snapshotBuilder.build(engine.world(), scenePath(), frame);
  }

  public StatsFrame buildStats() {
    World world = engine == null ? null : engine.world();
    int entityCount = world == null ? 0 : world.entityCount();
    float fps = 0f;
    if (Gdx.graphics != null) {
      fps = Gdx.graphics.getFramesPerSecond();
    }
    return new StatsFrame(fps, entityCount);
  }

  public String scenePath() {
    return HermesLauncherSupport.gameScenePath();
  }

  public void reloadScene() {
    if (engine == null) {
      return;
    }
    String path = scenePath();
    if (path != null && !path.isBlank()) {
      engine.loadScene(path);
    }
  }

  public void setComponentField(String entityId, String componentType, String field, Object value) {
    if (engine == null) {
      throw new IllegalStateException("Debug engine is not available");
    }
    ComponentRegistryImpl registry = engine.registryImpl();
    EntityId id = new EntityId(Long.parseLong(entityId));
    Entity entity = engine.world().getEntity(id);
    if (entity == null) {
      throw new IllegalArgumentException("Unknown entity: " + entityId);
    }
    Class<? extends Component> componentClass = registry.componentTypeFor(componentType);
    Component component = engine.world().getComponent(id, componentClass);
    if (component == null) {
      throw new IllegalArgumentException(
          "Entity '" + entityId + "' has no component '" + componentType + "'");
    }
    registry.applyField(component, field, value);
  }
}
