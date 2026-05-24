package dev.hermes.gradle.internal;

import dev.hermes.tooling.config.HermesConfigException;
import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.config.HermesGameConfigParser;

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

public final class HermesGameConfigs {

    private HermesGameConfigs() {
    }

    public static HermesGameConfig parse(Project gameProject) {
        return parse(gameProject.file("hermes.json"));
    }

    static HermesGameConfig parse(File file) {
        try {
            return HermesGameConfigParser.parse(file);
        } catch (HermesConfigException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }
}
