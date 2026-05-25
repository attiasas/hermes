package dev.hermes.core.config;

import dev.hermes.api.config.RuntimeConfigService;

public final class RuntimeConfigServices {

    private static RuntimeConfigService instance = new RuntimeConfigServiceImpl();

    private RuntimeConfigServices() {}

    public static RuntimeConfigService get() {
        return instance;
    }

    public static void install(RuntimeConfigService service) {
        instance = service;
    }
}
