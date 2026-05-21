package dev.hermes.debug;

import java.util.Map;

public record HdpMessage(
    String type,
    WorldSnapshot worldSnapshot,
    StatsFrame stats,
    Map<String, Object> command,
    String error) {

  public static HdpMessage worldSnapshot(WorldSnapshot s) {
    return new HdpMessage("worldSnapshot", s, null, null, null);
  }

  public static HdpMessage stats(StatsFrame s) {
    return new HdpMessage("stats", null, s, null, null);
  }

  public static HdpMessage error(String message) {
    return new HdpMessage("error", null, null, null, message);
  }
}
