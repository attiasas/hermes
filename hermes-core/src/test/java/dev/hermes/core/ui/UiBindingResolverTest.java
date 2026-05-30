package dev.hermes.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.ui.UiBindingProvider;
import dev.hermes.api.ui.UiNode;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class UiBindingResolverTest {

    @Test
    void setBindingOverridesProvider() {
        UiBindingResolver resolver = new UiBindingResolver();
        resolver.addProvider(key -> Optional.of(10));
        resolver.setBinding("player.hp", 42);

        assertEquals(42, resolver.resolve("player.hp"));
    }

    @Test
    void providerResolvesWhenNoExplicitBinding() {
        UiBindingResolver resolver = new UiBindingResolver();
        resolver.addProvider(key -> "player.hp".equals(key) ? Optional.of(75) : Optional.empty());

        assertEquals(75, resolver.resolve("player.hp"));
    }
}
