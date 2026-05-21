package dev.hermes.tooling.config;

/** Game/simulation fields from {@code hermes.json} (no build or platform knobs). */
public final class HermesGameConfig {

  private String title = "HermesGame";
  private String scene = "scenes/main.json";
  private String renderPipeline = "render/pipeline.json";

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getScene() {
    return scene;
  }

  public void setScene(String scene) {
    this.scene = scene;
  }

  public String getRenderPipeline() {
    return renderPipeline;
  }

  public void setRenderPipeline(String renderPipeline) {
    this.renderPipeline = renderPipeline;
  }
}
