package dev.hermes.api.log;

/** Discards all messages until a real provider is installed. */
final class NoopLoggerProvider implements LoggerProvider {

  static final LoggerProvider INSTANCE = new NoopLoggerProvider();

  private NoopLoggerProvider() {}

  @Override
  public Logger get(String category) {
    return new NoopLogger(category);
  }

  private static final class NoopLogger implements Logger {
    private final String category;

    NoopLogger(String category) {
      this.category = category;
    }

    @Override
    public void debug(String message) {}

    @Override
    public void error(String message) {}

    @Override
    public void info(String message) {}

    @Override
    public void warn(String message) {}

    @Override
    public void warn(String message, Throwable throwable) {}
    
    @Override
    public void error(String message, Throwable throwable) {}
  }
}