package dev.hermes.core.ui;

import dev.hermes.api.ui.UiCustomWidget;
import dev.hermes.api.ui.UiWidgetRegistry;

import java.util.HashMap;
import java.util.Map;

/** Custom widget type registry populated via {@link dev.hermes.api.ui.UiWidgetRegistration} SPI. */
final class UiWidgetRegistryImpl implements UiWidgetRegistry {

    private final Map<String, UiCustomWidgetImpl> registrations = new HashMap<>();

    @Override
    public void register(String type, UiCustomWidget widget) {
        if (type == null || type.isBlank() || widget == null) {
            return;
        }
        if (!(widget instanceof UiCustomWidgetImpl)) {
            throw new IllegalArgumentException(
                    "Custom widget must implement dev.hermes.core.ui.UiCustomWidgetImpl: "
                            + widget.getClass().getName());
        }
        registrations.put(type, (UiCustomWidgetImpl) widget);
    }

    @Override
    public boolean supports(String type) {
        return type != null && registrations.containsKey(type);
    }

    UiCustomWidgetImpl handler(String type) {
        return type == null ? null : registrations.get(type);
    }
}
