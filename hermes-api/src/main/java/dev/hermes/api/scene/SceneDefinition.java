package dev.hermes.api.scene;

import dev.hermes.api.ecs.System;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Declarative scene registration: id, content source, lifecycle, and scene-local systems. */
public final class SceneDefinition {

  private final String id;
  private final SceneSource source;
  private final SceneLifecycle lifecycle;
  private final List<System> systems;
  private final String renderPipeline;

  public SceneDefinition(String id, SceneSource source) {
    this(id, source, null, List.of(), null);
  }

  public SceneDefinition(
      String id, SceneSource source, SceneLifecycle lifecycle, List<System> systems) {
    this(id, source, lifecycle, systems, null);
  }

  public SceneDefinition(
      String id,
      SceneSource source,
      SceneLifecycle lifecycle,
      List<System> systems,
      String renderPipeline) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Scene id is required");
    }
    if (source == null) {
      throw new IllegalArgumentException("Scene source is required");
    }
    this.id = id;
    this.source = source;
    this.lifecycle = lifecycle;
    this.systems = List.copyOf(systems == null ? List.of() : systems);
    this.renderPipeline =
        renderPipeline == null || renderPipeline.isBlank() ? null : renderPipeline.trim();
  }

  public String id() {
    return id;
  }

  public SceneSource source() {
    return source;
  }

  public SceneLifecycle lifecycle() {
    return lifecycle;
  }

  public List<System> systems() {
    return systems;
  }

  /** Optional render pipeline asset path when no scene JSON override is present. */
  public Optional<String> renderPipeline() {
    return Optional.ofNullable(renderPipeline);
  }

  public static Builder builder(String id) {
    return new Builder(id);
  }

  public static final class Builder {

    private final String id;
    private SceneSource source;
    private SceneLifecycle lifecycle;
    private final List<System> systems = new ArrayList<>();
    private String renderPipeline;

    private Builder(String id) {
      if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("Scene id is required");
      }
      this.id = id;
    }

    public Builder source(SceneSource source) {
      this.source = source;
      return this;
    }

    public Builder lifecycle(SceneLifecycle lifecycle) {
      this.lifecycle = lifecycle;
      return this;
    }

    public Builder system(System system) {
      this.systems.add(system);
      return this;
    }

    public Builder systems(List<System> systems) {
      this.systems.clear();
      if (systems != null) {
        this.systems.addAll(systems);
      }
      return this;
    }

    public Builder renderPipeline(String renderPipeline) {
      this.renderPipeline = renderPipeline;
      return this;
    }

    public SceneDefinition build() {
      return new SceneDefinition(
          id, source, lifecycle, Collections.unmodifiableList(systems), renderPipeline);
    }
  }
}
