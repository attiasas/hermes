package dev.hermes.core.ui;

import dev.hermes.api.ui.UiBindingProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Resolves UI data-binding keys: explicit values win over {@link UiBindingProvider} callbacks. */
public final class UiBindingResolver {

    private final Map<String, Object> explicit = new HashMap<>();
    private final List<UiBindingProvider> providers = new ArrayList<>();

    public void setBinding(String key, Object value) {
        if (key != null && !key.isBlank()) {
            explicit.put(key, value);
        }
    }

    public void addProvider(UiBindingProvider provider) {
        if (provider != null) {
            providers.add(provider);
        }
    }

    public Object resolve(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (explicit.containsKey(key)) {
            return explicit.get(key);
        }
        for (UiBindingProvider provider : providers) {
            Optional<Object> resolved = provider.resolve(key);
            if (resolved.isPresent()) {
                return resolved.get();
            }
        }
        return null;
    }
}
