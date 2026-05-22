package dev.hermes.api.log;

public interface LoggerProvider {
    Logger get(String category);
}