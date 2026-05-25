package dev.hermes.api.log;

/**
 * Static access when {@link dev.hermes.api.ecs.HermesEngine} is not available.
 */
public final class Logs {

    private static volatile LoggerProvider provider = NoopLoggerProvider.INSTANCE;

    private Logs() {
    }

    public static void install(LoggerProvider newProvider) {
        provider = newProvider != null ? newProvider : NoopLoggerProvider.INSTANCE;
    }

    public static Logger get(Class<?> type) {
        return get(type.getName());
    }

    public static Logger get(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("log category is required");
        }
        return provider.get(category);
    }
}
