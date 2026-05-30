package dev.hermes.core.ui;

import java.util.Set;

/** Built-in widget types for ui-format v1 documents. */
public final class BuiltinUiWidgets {

    private static final Set<String> TYPES =
            Set.of("panel", "image", "label", "button", "progressBar", "spacer");

    public boolean supports(String type) {
        return type != null && TYPES.contains(type);
    }
}
