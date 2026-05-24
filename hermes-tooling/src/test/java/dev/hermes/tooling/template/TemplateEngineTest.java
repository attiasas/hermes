package dev.hermes.tooling.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

class TemplateEngineTest {

    @Test
    void substitute_replacesTokens() {
        String result =
                TemplateEngine.substitute(
                        "Hello {{NAME}} in {{DIR}}/{{package}}",
                        Map.of("{{NAME}}", "World", "{{DIR}}", "src", "{{package}}", "dev.hermes.game"));
        assertEquals("Hello World in src/dev.hermes.game", result);
    }

    @Test
    void substitute_emptyTokensReturnsInput() {
        assertEquals("no change", TemplateEngine.substitute("no change", Map.of()));
    }

    @Test
    void substitute_nullTokensReturnsInput() {
        assertEquals("no change", TemplateEngine.substitute("no change", null));
    }
}
