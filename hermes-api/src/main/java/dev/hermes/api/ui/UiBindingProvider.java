package dev.hermes.api.ui;

import java.util.Optional;

/**
 * Optional callback that resolves binding keys when not set explicitly on {@link UiService}.
 */
@FunctionalInterface
public interface UiBindingProvider {

    Optional<Object> resolve(String key);
}
