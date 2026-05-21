package dev.hermes.studio.app;

import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Path;
import org.eclipse.jetty.server.Server;

/** Hermes Studio desktop shell entry point. */
public final class StudioApp {

  private StudioApp() {}

  public static void main(String[] args) throws Exception {
    int port = 17890;
    Path project = null;
    for (int i = 0; i < args.length; i++) {
      if ("--port".equals(args[i]) && i + 1 < args.length) {
        port = Integer.parseInt(args[++i]);
      } else if ("--project".equals(args[i]) && i + 1 < args.length) {
        project = Path.of(args[++i]);
      }
    }

    StudioServer studio = new StudioServer(port);
    if (project != null) {
      studio.openProject(project);
    }
    Server server = studio.start();
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + port + "/"));
    }
    server.join();
  }
}
