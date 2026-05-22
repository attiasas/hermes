package dev.hermes.core.log;

import dev.hermes.api.log.Logger;
import dev.hermes.api.log.LoggerProvider;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class CachingLoggerProvider implements LoggerProvider {

  private final LogSink sink;
  private final ConcurrentHashMap<String, Logger> cache = new ConcurrentHashMap<>();

  public CachingLoggerProvider(LogSink sink) {
    this.sink = Objects.requireNonNull(sink, "sink");
  }

  @Override
  public Logger get(String category) {
    return cache.computeIfAbsent(category, c -> new LoggerImpl(c, sink));
  }
}