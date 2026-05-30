package dev.hermes.api.ui;

import dev.hermes.api.scene.SceneUiConfig;

import java.util.Optional;

/**
 * Custom UI service: document load/cache, per-scene activation, bindings, and widget registry.
 */
public interface UiService {

    UiDocument load(String assetPath);

    void onSceneEnter(String sceneId, Optional<SceneUiConfig> config);

    void onSceneExit(String sceneId);

    void setBinding(String key, Object value);

    Object getBinding(String key);

    void addBindingProvider(UiBindingProvider provider);

    UiWidgetRegistry widgets();
}
