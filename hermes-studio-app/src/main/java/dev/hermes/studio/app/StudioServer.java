package dev.hermes.studio.app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hermes.studio.FileTreeService;
import dev.hermes.studio.GradleProcessRunner;
import dev.hermes.studio.HermesProject;
import dev.hermes.studio.ProjectService;
import dev.hermes.studio.config.ConfigAggregator;
import dev.hermes.studio.config.HermesProjectConfigView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

/** Jetty server for Studio static UI and REST APIs. */
public final class StudioServer {

  private static final int DEBUG_PORT = 18765;

  private final int port;
  private final ProjectService projectService = new ProjectService();
  private final ConfigAggregator configAggregator = new ConfigAggregator();
  private final FileTreeService fileTreeService = new FileTreeService();
  private final Gson gson = new Gson();

  private HermesProject project;
  private GradleProcessRunner runner;

  public StudioServer(int port) {
    this.port = port;
  }

  public void openProject(Path root) {
    this.project = projectService.open(root);
    this.runner = new GradleProcessRunner(project);
  }

  public Server start() throws Exception {
    Server server = new Server(port);
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    context.setBaseResource(Resource.newClassPathResource("/studio-ui/"));
    context.addServlet(DefaultServlet.class, "/*");
    context.addServlet(new ServletHolder(new StudioApiServlet()), "/api/*");
    server.setHandler(context);
    server.start();
    return server;
  }

  private final class StudioApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      String path = request.getPathInfo();
      if (path == null) {
        sendError(response, 404, "Not found");
        return;
      }
      try {
        switch (path) {
          case "/play/port" -> writeJson(response, gson.toJsonTree(java.util.Map.of("port", DEBUG_PORT)));
          case "/config" -> handleConfigGet(response);
          case "/files/tree" -> handleFilesTree(response);
          default -> sendError(response, 404, "Not found");
        }
      } catch (Exception e) {
        sendError(response, 500, e.getMessage());
      }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      String path = request.getPathInfo();
      if (path == null) {
        sendError(response, 404, "Not found");
        return;
      }
      try {
        switch (path) {
          case "/project/open" -> handleProjectOpen(request, response);
          case "/play/run" -> handlePlayRun(response);
          case "/play/stop" -> handlePlayStop(response);
          case "/files/open" -> handleFilesOpen(request, response);
          default -> sendError(response, 404, "Not found");
        }
      } catch (Exception e) {
        sendError(response, 500, e.getMessage());
      }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      if ("PATCH".equalsIgnoreCase(request.getMethod())) {
        if (!"/config".equals(request.getPathInfo())) {
          sendError(response, 404, "Not found");
          return;
        }
        try {
          handleConfigPatch(request, response);
        } catch (Exception e) {
          sendError(response, 500, e.getMessage());
        }
        return;
      }
      super.service(request, response);
    }

    private void handleProjectOpen(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
      JsonObject body = readJsonObject(request);
      if (!body.has("path")) {
        sendError(response, 400, "path is required");
        return;
      }
      openProject(Path.of(body.get("path").getAsString()));
      writeJson(response, gson.toJsonTree(java.util.Map.of("ok", true)));
    }

    private void handlePlayRun(HttpServletResponse response) throws IOException {
      if (!requireProject(response)) {
        return;
      }
      if (runner == null) {
        runner = new GradleProcessRunner(project);
      }
      runner.start();
      writeJson(response, gson.toJsonTree(java.util.Map.of("ok", true)));
    }

    private void handlePlayStop(HttpServletResponse response) throws IOException {
      if (runner != null) {
        runner.stop();
      }
      writeJson(response, gson.toJsonTree(java.util.Map.of("ok", true)));
    }

    private void handleFilesOpen(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
      if (!requireProject(response)) {
        return;
      }
      JsonObject body = readJsonObject(request);
      if (!body.has("path")) {
        sendError(response, 400, "path is required");
        return;
      }
      Path file = resolveProjectPath(body.get("path").getAsString());
      if (!Files.isRegularFile(file)) {
        sendError(response, 404, "File not found");
        return;
      }
      if (!Desktop.isDesktopSupported()) {
        sendError(response, 501, "Desktop integration unavailable");
        return;
      }
      Desktop.getDesktop().open(file.toFile());
      writeJson(response, gson.toJsonTree(java.util.Map.of("ok", true)));
    }

    private void handleFilesTree(HttpServletResponse response) throws IOException {
      if (!requireProject(response)) {
        return;
      }
      writeJson(response, gson.toJsonTree(fileTreeService.list(project.root())));
    }

    private void handleConfigGet(HttpServletResponse response) throws IOException {
      if (!requireProject(response)) {
        return;
      }
      HermesProjectConfigView view = configAggregator.load(project.root());
      writeJson(response, gson.toJsonTree(view));
    }

    private void handleConfigPatch(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
      if (!requireProject(response)) {
        return;
      }
      JsonObject patch = readJsonObject(request);
      HermesProjectConfigView current = configAggregator.load(project.root());
      HermesProjectConfigView merged = configAggregator.merge(current, patch);
      configAggregator.save(project.root(), merged);
      writeJson(response, gson.toJsonTree(merged));
    }

    private boolean requireProject(HttpServletResponse response) throws IOException {
      if (project == null) {
        sendError(response, 400, "No project open. POST /api/project/open first.");
        return false;
      }
      return true;
    }

    private Path resolveProjectPath(String path) {
      Path resolved = Path.of(path);
      if (resolved.isAbsolute()) {
        return resolved.normalize();
      }
      return project.root().resolve(path).normalize();
    }
  }

  private static JsonObject readJsonObject(HttpServletRequest request) throws IOException {
    try (Reader reader =
        new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8)) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    }
  }

  private static void writeJson(HttpServletResponse response, com.google.gson.JsonElement body)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().write(new Gson().toJson(body));
  }

  private static void sendError(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"" + message.replace("\"", "\\\"") + "\"}");
  }
}
