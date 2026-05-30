package dev.hermes.api.ui;

/**
 * Registry for custom widget type deserializers and render hooks (SPI extensions).
 */
public interface UiWidgetRegistry {

    void register(String type, UiCustomWidget widget);

    boolean supports(String type);
}
