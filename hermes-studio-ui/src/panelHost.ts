export interface Panel {
  id: string;
  title: string;
  mount(container: HTMLElement): void;
}

export class PanelHost {
  private readonly panels = new Map<string, Panel>();

  registerPanel(panel: Panel): void {
    this.panels.set(panel.id, panel);
  }

  mountAll(container: HTMLElement): void {
    for (const panel of this.panels.values()) {
      const shell = document.createElement("div");
      shell.className = "panel";
      shell.dataset.panelId = panel.id;
      const heading = document.createElement("h2");
      heading.textContent = panel.title;
      shell.appendChild(heading);
      const body = document.createElement("div");
      body.className = "panel-body";
      shell.appendChild(body);
      container.appendChild(shell);
      panel.mount(body);
    }
  }

  getPanel(id: string): Panel | undefined {
    return this.panels.get(id);
  }
}
