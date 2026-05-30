package dev.hermes.core.ui;

import dev.hermes.api.scene.SceneUiConfig;
import dev.hermes.api.ui.UiBindingProvider;
import dev.hermes.api.ui.UiDocument;
import dev.hermes.api.ui.UiService;
import dev.hermes.api.ui.UiWidgetRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** {@link UiService} implementation; layout and rendering arrive in later tasks. */
public final class UiServiceImpl implements UiService {

    private final UiDocumentLoader documentLoader = new UiDocumentLoader(new BuiltinUiWidgets());
    private final UiWidgetRegistryImpl widgets = new UiWidgetRegistryImpl();
    private final Map<String, Object> bindings = new HashMap<>();
    private final List<UiBindingProvider> bindingProviders = new ArrayList<>();

    @Override
    public UiDocument load(String assetPath) {
        return documentLoader.load(assetPath);
    }

    @Override
    public void onSceneEnter(String sceneId, Optional<SceneUiConfig> config) {
    }

    @Override
    public void onSceneExit(String sceneId) {
    }

    @Override
    public void setBinding(String key, Object value) {
        if (key != null && !key.isBlank()) {
            bindings.put(key, value);
        }
    }

    @Override
    public Object getBinding(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (bindings.containsKey(key)) {
            return bindings.get(key);
        }
        for (UiBindingProvider provider : bindingProviders) {
            Optional<Object> resolved = provider.resolve(key);
            if (resolved.isPresent()) {
                return resolved.get();
            }
        }
        return null;
    }

    @Override
    public void addBindingProvider(UiBindingProvider provider) {
        if (provider != null) {
            bindingProviders.add(provider);
        }
    }

    @Override
    public UiWidgetRegistry widgets() {
        return widgets;
    }
}
