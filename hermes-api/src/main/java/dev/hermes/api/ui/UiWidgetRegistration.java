package dev.hermes.api.ui;

/**
 * ServiceLoader entry for custom widget types (deserialize props and draw hooks).
 */
public interface UiWidgetRegistration {

    void register(UiWidgetRegistry registry);
}
