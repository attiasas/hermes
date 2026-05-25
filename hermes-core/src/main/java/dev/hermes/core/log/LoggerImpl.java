package dev.hermes.core.log;

import java.util.Objects;

import dev.hermes.api.log.LogLevel;
import dev.hermes.api.log.Logger;

public final class LoggerImpl implements Logger {

    private final String category;
    private final LogSink sink;

    public LoggerImpl(String category, LogSink sink) {
        this.category = Objects.requireNonNull(category, "category");
        this.sink = Objects.requireNonNull(sink, "sink");
        if (category.isBlank()) {
            throw new IllegalArgumentException("log category is required");
        }
    }

    @Override
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    @Override
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    @Override
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    @Override
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    private void log(LogLevel level, String message, Throwable throwable) {
        if (!LogConfig.isEnabled(level)) {
            return;
        }
        if (!LogConfig.isMatched(category)) {
            return;
        }
        sink.log(level, category, message, throwable);
    }
}
