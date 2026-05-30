package dev.hermes.core.ui;

import dev.hermes.api.ui.UiWidgetRegistration;
import dev.hermes.api.ui.UiWidgetRegistry;

import java.util.HashMap;
import java.util.Map;

/** Empty widget registry stub; populated when custom widget SPI lands. */
final class UiWidgetRegistryImpl implements UiWidgetRegistry {

    private final Map<String, UiWidgetRegistration> registrations = new HashMap<>();

    @Override
    public void register(String type, UiWidgetRegistration registration) {
        if (type != null && !type.isBlank() && registration != null) {
            registrations.put(type, registration);
        }
    }
}
