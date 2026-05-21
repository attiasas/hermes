package dev.hermes.core.render.resource;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import dev.hermes.core.HermesAssetPaths;
import java.util.HashMap;
import java.util.Map;

/** Loads and caches Wavefront OBJ models from game assets. */
public final class ModelCache {

  private final ObjLoader loader = new ObjLoader();
  private final Map<String, Model> models = new HashMap<>();

  public Model get(String modelPath) {
    if (modelPath == null || modelPath.isBlank()) {
      throw new IllegalArgumentException("model path must not be blank");
    }
    return models.computeIfAbsent(modelPath, path -> loader.loadModel(HermesAssetPaths.internal(path)));
  }

  public void dispose() {
    for (Model model : models.values()) {
      model.dispose();
    }
    models.clear();
  }
}
