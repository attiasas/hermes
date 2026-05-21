package dev.hermes.core.debug;

import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hermes.debug.HdpMessage;
import dev.hermes.debug.StatsFrame;
import dev.hermes.debug.WorldSnapshot;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/** Localhost WebSocket server for Hermes Debug Protocol (HDP) v0. */
public final class HermesDebugServer {

  private static final long BROADCAST_INTERVAL_MS = 150L;

  private final DebugRuntime runtime;
  private final int port;
  private final Gson gson = new Gson();
  private final AtomicLong frame = new AtomicLong();
  private final AtomicBoolean listening = new AtomicBoolean(false);

  private WebSocketServer server;
  private Thread broadcastThread;

  public HermesDebugServer(DebugRuntime runtime, int port) {
    this.runtime = runtime;
    this.port = port;
  }

  public void startIfEnabled() {
    if (!runtime.isDebugEnabled()) {
      return;
    }
    server =
        new WebSocketServer(new InetSocketAddress("127.0.0.1", port)) {
          @Override
          public void onOpen(WebSocket conn, ClientHandshake handshake) {
            sendLatest(conn);
          }

          @Override
          public void onMessage(WebSocket conn, String message) {
            handleCommand(message);
          }

          @Override
          public void onStart() {}

          @Override
          public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

          @Override
          public void onError(WebSocket conn, Exception ex) {
            // Best-effort debug channel; errors are surfaced to clients when possible.
          }
        };
    server.start();
    listening.set(true);
    startBroadcastThread();
  }

  public boolean isListening() {
    return listening.get();
  }

  public void stop() {
    listening.set(false);
    if (broadcastThread != null) {
      broadcastThread.interrupt();
      broadcastThread = null;
    }
    if (server != null) {
      try {
        server.stop();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      server = null;
    }
  }

  private void startBroadcastThread() {
    broadcastThread =
        new Thread(
            () -> {
              while (listening.get() && !Thread.currentThread().isInterrupted()) {
                broadcastLatest();
                try {
                  Thread.sleep(BROADCAST_INTERVAL_MS);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  break;
                }
              }
            },
            "hermes-debug-broadcast");
    broadcastThread.setDaemon(true);
    broadcastThread.start();
  }

  private void broadcastLatest() {
    if (server == null) {
      return;
    }
    long currentFrame = frame.incrementAndGet();
    WorldSnapshot snapshot = runtime.buildWorldSnapshot(currentFrame);
    StatsFrame stats = runtime.buildStats();
    String statsJson = gson.toJson(HdpMessage.stats(stats));
    String snapshotJson = gson.toJson(HdpMessage.worldSnapshot(snapshot));
    Collection<WebSocket> connections = server.getConnections();
    for (WebSocket conn : connections) {
      if (conn.isOpen()) {
        conn.send(statsJson);
        conn.send(snapshotJson);
      }
    }
  }

  private void sendLatest(WebSocket conn) {
    long currentFrame = frame.get();
    WorldSnapshot snapshot = runtime.buildWorldSnapshot(currentFrame);
    StatsFrame stats = runtime.buildStats();
    conn.send(gson.toJson(HdpMessage.stats(stats)));
    conn.send(gson.toJson(HdpMessage.worldSnapshot(snapshot)));
  }

  private void handleCommand(String message) {
    JsonObject root;
    try {
      root = JsonParser.parseString(message).getAsJsonObject();
    } catch (RuntimeException e) {
      broadcastError("Invalid command JSON");
      return;
    }
    if (!root.has("type") || !root.get("type").isJsonPrimitive()) {
      broadcastError("Command type is required");
      return;
    }
    String type = root.get("type").getAsString();
    switch (type) {
      case "pause":
        postRunnable(() -> runtime.setPaused(true));
        break;
      case "resume":
        postRunnable(() -> runtime.setPaused(false));
        break;
      case "reloadScene":
        postRunnable(runtime::reloadScene);
        break;
      case "setComponentField":
        handleSetComponentField(root);
        break;
      default:
        broadcastError("Unknown command: " + type);
    }
  }

  private void handleSetComponentField(JsonObject root) {
    if (!root.has("entityId") || !root.has("componentType") || !root.has("field")) {
      broadcastError("setComponentField requires entityId, componentType, and field");
      return;
    }
    String entityId = root.get("entityId").getAsString();
    String componentType = root.get("componentType").getAsString();
    String field = root.get("field").getAsString();
    Object value =
        root.has("value") && !root.get("value").isJsonNull()
            ? gson.fromJson(root.get("value"), Object.class)
            : null;
    postRunnable(
        () -> {
          try {
            runtime.setComponentField(entityId, componentType, field, value);
          } catch (RuntimeException e) {
            broadcastError(e.getMessage());
          }
        });
  }

  private void postRunnable(Runnable action) {
    if (Gdx.app != null) {
      Gdx.app.postRunnable(action);
    } else {
      action.run();
    }
  }

  private void broadcastError(String message) {
    if (server == null) {
      return;
    }
    String json = gson.toJson(HdpMessage.error(message));
    for (WebSocket conn : server.getConnections()) {
      if (conn.isOpen()) {
        conn.send(json);
      }
    }
  }
}
