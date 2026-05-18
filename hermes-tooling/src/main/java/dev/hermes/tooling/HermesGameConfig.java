package dev.hermes.tooling;

/** Game/simulation fields from {@code hermes.json} (no build or platform knobs). */
public final class HermesGameConfig {

  private String name = "HermesGame";
  private String version = "0.1.0";
  private String scene = "scenes/main.json";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getScene() {
    return scene;
  }

  public void setScene(String scene) {
    this.scene = scene;
  }
}
