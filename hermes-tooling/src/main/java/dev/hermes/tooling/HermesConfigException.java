package dev.hermes.tooling;

public final class HermesConfigException extends RuntimeException {

  public HermesConfigException(String message) {
    super(message);
  }

  public HermesConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
