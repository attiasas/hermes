package dev.hermes.studio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Runs {@code ./gradlew :game:hermesRunDesktop} in a Hermes project root. */
public final class GradleProcessRunner {

  private final Path root;
  private final List<Consumer<String>> stdoutListeners = new ArrayList<>();
  private final List<Consumer<String>> stderrListeners = new ArrayList<>();
  private Process process;
  private ExecutorService streamExecutor;

  public GradleProcessRunner(HermesProject project) {
    this(project.root());
  }

  public GradleProcessRunner(Path root) {
    this.root = root.toAbsolutePath().normalize();
  }

  public void addStdoutListener(Consumer<String> listener) {
    stdoutListeners.add(listener);
  }

  public void addStderrListener(Consumer<String> listener) {
    stderrListeners.add(listener);
  }

  public synchronized void start() throws IOException {
    if (process != null && process.isAlive()) {
      return;
    }
    ProcessBuilder builder =
        new ProcessBuilder("./gradlew", ":game:hermesRunDesktop").directory(root.toFile());
    builder.redirectErrorStream(false);
    process = builder.start();
    streamExecutor = Executors.newFixedThreadPool(2);
    streamExecutor.submit(() -> pump(process.getInputStream(), stdoutListeners));
    streamExecutor.submit(() -> pump(process.getErrorStream(), stderrListeners));
  }

  public synchronized void stop() {
    if (process == null) {
      return;
    }
    Process active = process;
    active.descendants().forEach(ProcessHandle::destroyForcibly);
    active.destroyForcibly();
    process = null;
    if (streamExecutor != null) {
      streamExecutor.shutdownNow();
      streamExecutor = null;
    }
  }

  public boolean isRunning() {
    return process != null && process.isAlive();
  }

  private static void pump(java.io.InputStream stream, List<Consumer<String>> listeners) {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        for (Consumer<String> listener : listeners) {
          listener.accept(line);
        }
      }
    } catch (IOException ignored) {
      // Process ended or stream closed.
    }
  }
}
