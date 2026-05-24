package dev.hermes.core.log;

/** Reinitializes logging filters after runtime configuration is applied. */
public final class LoggingRuntime {

    private LoggingRuntime() {}

    public static void reinitialize() {
        LogConfig.reinitialize();
    }
}
