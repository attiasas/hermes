package dev.hermes.api.scene;

/** Queued scene transition processed by {@link SceneManager#processPending()}. */
public final class SceneChangeRequest {

  public enum Kind {
    GO_TO,
    PUSH,
    POP
  }

  private final Kind kind;
  private final String sceneId;

  private SceneChangeRequest(Kind kind, String sceneId) {
    this.kind = kind;
    this.sceneId = sceneId;
  }

  public Kind kind() {
    return kind;
  }

  /** Target scene id for {@link Kind#GO_TO} and {@link Kind#PUSH}; {@code null} for {@link Kind#POP}. */
  public String sceneId() {
    return sceneId;
  }

  public static SceneChangeRequest goTo(String sceneId) {
    return new SceneChangeRequest(Kind.GO_TO, sceneId);
  }

  public static SceneChangeRequest push(String sceneId) {
    return new SceneChangeRequest(Kind.PUSH, sceneId);
  }

  public static SceneChangeRequest pop() {
    return new SceneChangeRequest(Kind.POP, null);
  }
}
