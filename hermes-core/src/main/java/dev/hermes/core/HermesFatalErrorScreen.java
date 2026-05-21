package dev.hermes.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.List;

/** Draws a white fullscreen panel with wrapped error text after a fatal engine failure. */
public final class HermesFatalErrorScreen implements Disposable {

  private static final int MAX_MESSAGE_CHARS = 6000;
  private static final int WRAP_CHARS = 72;

  private SpriteBatch batch;
  private BitmapFont font;
  private String message;
  private List<String> lines = List.of();

  public boolean isActive() {
    return message != null;
  }

  public void report(Throwable error) {
    if (message != null) {
      return;
    }
    message = format(error);
    lines = wrap(message, WRAP_CHARS);
    Gdx.app.error("Hermes", message, error);
  }

  public void render(int width, int height) {
    ensureGraphics();
    Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    if (lines.isEmpty()) {
      return;
    }
    font.setColor(0.1f, 0.1f, 0.1f, 1f);
    float y = height - 24f;
    float lineHeight = font.getLineHeight() + 2f;
    batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    batch.begin();
    for (String line : lines) {
      if (y < 8f) {
        break;
      }
      font.draw(batch, line, 16f, y);
      y -= lineHeight;
    }
    batch.end();
  }

  @Override
  public void dispose() {
    if (batch != null) {
      batch.dispose();
      batch = null;
    }
    if (font != null) {
      font.dispose();
      font = null;
    }
  }

  private void ensureGraphics() {
    if (batch == null) {
      batch = new SpriteBatch();
      font = new BitmapFont();
    }
  }

  private static String format(Throwable error) {
    StringBuilder sb = new StringBuilder();
    sb.append("Hermes engine error\n\n");
    Throwable current = error;
    while (current != null && sb.length() < MAX_MESSAGE_CHARS) {
      if (current.getMessage() != null && !current.getMessage().isBlank()) {
        sb.append(current.getClass().getSimpleName()).append(": ").append(current.getMessage());
      } else {
        sb.append(current.getClass().getName());
      }
      sb.append('\n');
      for (StackTraceElement frame : current.getStackTrace()) {
        if (sb.length() >= MAX_MESSAGE_CHARS) {
          break;
        }
        sb.append("    at ").append(frame).append('\n');
      }
      current = current.getCause();
      if (current != null) {
        sb.append("Caused by: ");
      }
    }
    return sb.toString();
  }

  private static List<String> wrap(String text, int maxChars) {
    List<String> result = new ArrayList<>();
    for (String raw : text.split("\n", -1)) {
      wrapLine(raw, maxChars, result);
    }
    return result;
  }

  private static void wrapLine(String raw, int maxChars, List<String> result) {
    String line = raw;
    while (line.length() > maxChars) {
      int breakAt = line.lastIndexOf(' ', maxChars);
      if (breakAt <= 0) {
        breakAt = maxChars;
      }
      result.add(line.substring(0, breakAt).trim());
      line = line.substring(breakAt).trim();
    }
    result.add(line);
  }
}
