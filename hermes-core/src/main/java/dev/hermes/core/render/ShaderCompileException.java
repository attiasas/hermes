package dev.hermes.core.render;

/** Thrown when a pipeline shader fails to load or compile. */
public final class ShaderCompileException extends RuntimeException {

  public ShaderCompileException(String message) {
    super(message);
  }

  public ShaderCompileException(String message, Throwable cause) {
    super(message, cause);
  }
}
