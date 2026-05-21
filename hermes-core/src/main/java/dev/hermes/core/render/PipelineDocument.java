package dev.hermes.core.render;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parsed render pipeline JSON (version 1). */
public final class PipelineDocument {

  public static final int SUPPORTED_VERSION = 1;

  public enum PassType {
    WORLD3D("world3d"),
    SPRITES("sprites"),
    UI("ui"),
    CUSTOM("custom");

    private final String jsonName;

    PassType(String jsonName) {
      this.jsonName = jsonName;
    }

    static PassType fromJson(String value) {
      if (value == null || value.isBlank()) {
        throw new PipelineParseException("pass type is required");
      }
      for (PassType type : values()) {
        if (type.jsonName.equals(value)) {
          return type;
        }
      }
      throw new PipelineParseException("unknown pass type: " + value);
    }
  }

  public static final class FramebufferDef {
    private final String id;
    private final int width;
    private final int height;
    private final boolean depth;

    public FramebufferDef(String id, int width, int height, boolean depth) {
      this.id = id;
      this.width = width;
      this.height = height;
      this.depth = depth;
    }

    public String id() {
      return id;
    }

    public int width() {
      return width;
    }

    public int height() {
      return height;
    }

    public boolean depth() {
      return depth;
    }
  }

  public static final class ShaderDef {
    private final String vertex;
    private final String fragment;

    public ShaderDef(String vertex, String fragment) {
      this.vertex = vertex;
      this.fragment = fragment;
    }

    public String vertex() {
      return vertex;
    }

    public String fragment() {
      return fragment;
    }
  }

  public static final class PassDef {
    private final String id;
    private final PassType type;
    private final String handler;
    private final String target;
    private final List<String> layers;
    private final boolean depthTest;

    public PassDef(
        String id,
        PassType type,
        String handler,
        String target,
        List<String> layers,
        boolean depthTest) {
      this.id = id;
      this.type = type;
      this.handler = handler;
      this.target = target;
      this.layers = layers;
      this.depthTest = depthTest;
    }

    public String id() {
      return id;
    }

    public PassType type() {
      return type;
    }

    /** Handler id for {@link PassType#CUSTOM}; {@code null} for built-in pass types. */
    public String handler() {
      return handler;
    }

    public String target() {
      return target;
    }

    public List<String> layers() {
      return layers;
    }

    public boolean depthTest() {
      return depthTest;
    }
  }

  private final int version;
  private final float[] clearColor;
  private final List<FramebufferDef> framebuffers;
  private final Map<String, ShaderDef> shaders;
  private final List<PassDef> passes;

  private PipelineDocument(
      int version,
      float[] clearColor,
      List<FramebufferDef> framebuffers,
      Map<String, ShaderDef> shaders,
      List<PassDef> passes) {
    this.version = version;
    this.clearColor = clearColor;
    this.framebuffers = List.copyOf(framebuffers);
    this.shaders = Map.copyOf(shaders);
    this.passes = List.copyOf(passes);
  }

  public int version() {
    return version;
  }

  public float[] clearColor() {
    return clearColor.clone();
  }

  public List<FramebufferDef> framebuffers() {
    return framebuffers;
  }

  public Map<String, ShaderDef> shaders() {
    return shaders;
  }

  public List<PassDef> passes() {
    return passes;
  }

  public static PipelineDocument parse(String json) {
    if (json == null || json.isBlank()) {
      throw new PipelineParseException("pipeline document is empty");
    }
    try {
      JsonValue root = new JsonReader().parse(json);
      return parseRoot(root);
    } catch (PipelineParseException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new PipelineParseException("invalid pipeline JSON: " + e.getMessage(), e);
    }
  }

  private static PipelineDocument parseRoot(JsonValue root) {
    if (root == null || !root.isObject()) {
      throw new PipelineParseException("pipeline root must be a JSON object");
    }

    int version = root.getInt("version", -1);
    if (version != SUPPORTED_VERSION) {
      throw new PipelineParseException("unsupported pipeline version: " + version);
    }

    float[] clearColor = parseClearColor(root.get("clearColor"));
    List<FramebufferDef> framebuffers = parseFramebuffers(root.get("framebuffers"));
    Map<String, ShaderDef> shaders = parseShaders(root.get("shaders"));
    List<PassDef> passes = parsePasses(root.get("passes"));

    return new PipelineDocument(version, clearColor, framebuffers, shaders, passes);
  }

  private static float[] parseClearColor(JsonValue value) {
    if (value == null || !value.isArray() || value.size != 4) {
      return new float[] {0f, 0f, 0f, 1f};
    }
    return new float[] {
      value.getFloat(0), value.getFloat(1), value.getFloat(2), value.getFloat(3)
    };
  }

  private static List<FramebufferDef> parseFramebuffers(JsonValue value) {
    if (value == null || !value.isArray()) {
      return List.of();
    }
    List<FramebufferDef> result = new ArrayList<>();
    for (JsonValue entry : value) {
      if (!entry.isObject()) {
        throw new PipelineParseException("framebuffer entry must be an object");
      }
      String id = requireString(entry, "id", "framebuffer");
      int width = entry.getInt("width", 0);
      int height = entry.getInt("height", 0);
      boolean depth = entry.getBoolean("depth", false);
      result.add(new FramebufferDef(id, width, height, depth));
    }
    return result;
  }

  private static Map<String, ShaderDef> parseShaders(JsonValue value) {
    if (value == null || !value.isObject()) {
      return Map.of();
    }
    Map<String, ShaderDef> result = new LinkedHashMap<>();
    for (JsonValue entry : value) {
      if (!entry.isObject()) {
        throw new PipelineParseException("shader entry must be an object: " + entry.name);
      }
      String vertex = requireString(entry, "vertex", "shader " + entry.name);
      String fragment = requireString(entry, "fragment", "shader " + entry.name);
      result.put(entry.name, new ShaderDef(vertex, fragment));
    }
    return result;
  }

  private static List<PassDef> parsePasses(JsonValue value) {
    if (value == null || !value.isArray()) {
      return List.of();
    }
    List<PassDef> result = new ArrayList<>();
    for (JsonValue entry : value) {
      if (!entry.isObject()) {
        throw new PipelineParseException("pass entry must be an object");
      }
      String id = requireString(entry, "id", "pass");
      PassType type = PassType.fromJson(entry.getString("type", ""));
      String handler = parseHandler(entry, type);
      String target = entry.getString("target", "screen");
      List<String> layers = parseLayers(entry.get("layers"));
      boolean depthTest = entry.getBoolean("depthTest", true);
      result.add(new PassDef(id, type, handler, target, layers, depthTest));
    }
    return result;
  }

  private static String parseHandler(JsonValue entry, PassType type) {
    if (type != PassType.CUSTOM) {
      return null;
    }
    return requireString(entry, "handler", "custom pass");
  }

  private static List<String> parseLayers(JsonValue value) {
    if (value == null || !value.isArray()) {
      return List.of("WORLD");
    }
    List<String> layers = new ArrayList<>();
    for (JsonValue layer : value) {
      layers.add(layer.asString());
    }
    return Collections.unmodifiableList(layers);
  }

  private static String requireString(JsonValue object, String field, String context) {
    JsonValue value = object.get(field);
    if (value == null || !value.isString() || value.asString().isBlank()) {
      throw new PipelineParseException(context + " requires non-empty \"" + field + "\"");
    }
    return value.asString();
  }
}
