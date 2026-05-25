package dev.hermes.core.log;

import dev.hermes.api.log.LogLevel;

public interface LogSink {
    void log(LogLevel level, String category, String message, Throwable throwable);
}