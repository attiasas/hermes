package dev.hermes.core.ui;

/** Built-in and SPI-registered widget type names accepted by {@link UiDocumentLoader}. */
final class UiWidgetTypes {

    private final BuiltinUiWidgets builtins;
    private final UiWidgetRegistryImpl registry;

    UiWidgetTypes(BuiltinUiWidgets builtins, UiWidgetRegistryImpl registry) {
        this.builtins = builtins;
        this.registry = registry;
    }

    boolean supports(String type) {
        return builtins.supports(type) || registry.supports(type);
    }
}
