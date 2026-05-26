package dev.hermes.gradle.launcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.hermes.gradle.dsl.HermesConfig;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.junit.jupiter.api.Test;

class HermesGameModuleTest {

    @Test
    void resolveName_requiresSettingsProperty() {
        Project root = mock(Project.class);
        ExtraPropertiesExtension extra = mock(ExtraPropertiesExtension.class);
        when(root.getExtensions()).thenReturn(mock(org.gradle.api.plugins.ExtensionContainer.class));
        when(root.getExtensions().getExtraProperties()).thenReturn(extra);
        when(extra.has(HermesConfig.GAME_MODULE_PROPERTY)).thenReturn(false);

        assertThrows(GradleException.class, () -> HermesGameModule.resolveName(root));
    }

    @Test
    void resolveName_usesSettingsProperty() {
        Project root = mock(Project.class);
        ExtraPropertiesExtension extra = mock(ExtraPropertiesExtension.class);
        when(root.getExtensions()).thenReturn(mock(org.gradle.api.plugins.ExtensionContainer.class));
        when(root.getExtensions().getExtraProperties()).thenReturn(extra);
        when(extra.has(HermesConfig.GAME_MODULE_PROPERTY)).thenReturn(true);
        when(extra.get(HermesConfig.GAME_MODULE_PROPERTY)).thenReturn("dogfood-simulation");

        assertEquals("dogfood-simulation", HermesGameModule.resolveName(root));
    }
}
