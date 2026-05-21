package dev.hermes.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Creates and resizes off-screen framebuffers declared in a render pipeline. */
public final class FramebufferPool {

  private final List<PipelineDocument.FramebufferDef> defs;
  private final boolean allocateGpu;
  private final Map<String, FrameBuffer> buffers = new LinkedHashMap<>();
  private final List<String> allocationOrder = new ArrayList<>();
  private int windowWidth = 1;
  private int windowHeight = 1;
  private String lastBoundTarget;

  public FramebufferPool(List<PipelineDocument.FramebufferDef> defs) {
    this(defs, true);
  }

  FramebufferPool(List<PipelineDocument.FramebufferDef> defs, boolean allocateGpu) {
    this.defs = List.copyOf(Objects.requireNonNull(defs, "defs"));
    this.allocateGpu = allocateGpu;
    validateUniqueIds();
  }

  public List<String> framebufferIds() {
    return defs.stream().map(PipelineDocument.FramebufferDef::id).collect(Collectors.toList());
  }

  /** Framebuffer ids in the order they were first allocated on the last {@link #resize(int, int)}. */
  public List<String> allocationOrder() {
    return List.copyOf(allocationOrder);
  }

  /** Last target passed to {@link #beginPass(String)} (for tests). */
  public String lastBoundTarget() {
    return lastBoundTarget;
  }

  public void resize(int width, int height) {
    windowWidth = Math.max(1, width);
    windowHeight = Math.max(1, height);
    allocationOrder.clear();
    for (PipelineDocument.FramebufferDef def : defs) {
      int w = resolveDimension(def.width(), windowWidth);
      int h = resolveDimension(def.height(), windowHeight);
      FrameBuffer existing = buffers.get(def.id());
      if (existing == null) {
        if (allocateGpu) {
          buffers.put(def.id(), createBuffer(def, w, h));
        }
        allocationOrder.add(def.id());
      } else if (existing.getWidth() != w || existing.getHeight() != h) {
        existing.dispose();
        buffers.put(def.id(), createBuffer(def, w, h));
        allocationOrder.add(def.id());
      }
    }
  }

  public void beginPass(String target) {
    lastBoundTarget = target;
    if ("screen".equals(target)) {
      if (allocateGpu) {
        FrameBuffer.unbind();
        Gdx.gl.glViewport(0, 0, windowWidth, windowHeight);
      }
      return;
    }
    if (!allocateGpu) {
      return;
    }
    FrameBuffer buffer = buffers.get(target);
    if (buffer == null) {
      throw new IllegalStateException("framebuffer not allocated: " + target);
    }
    buffer.begin();
  }

  public void endPass(String target) {
    if ("screen".equals(target) || !allocateGpu) {
      return;
    }
    FrameBuffer buffer = buffers.get(target);
    if (buffer != null) {
      buffer.end();
    }
  }

  public void dispose() {
    for (FrameBuffer buffer : buffers.values()) {
      buffer.dispose();
    }
    buffers.clear();
    allocationOrder.clear();
  }

  private FrameBuffer createBuffer(PipelineDocument.FramebufferDef def, int width, int height) {
    return new FrameBuffer(Pixmap.Format.RGBA8888, width, height, def.depth());
  }

  private static int resolveDimension(int configured, int windowDimension) {
    return configured <= 0 ? windowDimension : configured;
  }

  private void validateUniqueIds() {
    java.util.Set<String> seen = new java.util.LinkedHashSet<>();
    for (PipelineDocument.FramebufferDef def : defs) {
      if (!seen.add(def.id())) {
        throw new PipelineParseException("duplicate framebuffer id: " + def.id());
      }
    }
  }
}
