import type { Panel } from "./panelHost";

export type HermesProjectConfigView = {
  game: { title: string; scene: string; sourceFile: string };
  project: {
    applicationClass: string;
    assetsDirectory: string;
    debug: boolean;
    version: string;
    sourceFile: string;
  };
  platforms: {
    desktop: PlatformEntry;
    html: PlatformEntry;
    android: PlatformEntry;
    sourceFile: string;
  };
};

type PlatformEntry = { enabled: boolean; width: number | null; height: number | null };

export class ConfigPanel implements Panel {
  readonly id = "config";
  readonly title = "Project Config";

  private container!: HTMLElement;
  private baseline: HermesProjectConfigView | null = null;
  private draft: HermesProjectConfigView | null = null;
  private dirtyIndicator!: HTMLElement;

  mount(container: HTMLElement): void {
    this.container = container;
    this.container.innerHTML = `
      <div id="config-toolbar">
        <button type="button" id="config-save">Save</button>
        <button type="button" id="config-revert">Revert</button>
        <span id="config-dirty"></span>
      </div>
      <div id="config-sections"></div>
    `;
    this.dirtyIndicator = this.container.querySelector("#config-dirty")!;
    this.container.querySelector("#config-save")!.addEventListener("click", () => void this.save());
    this.container.querySelector("#config-revert")!.addEventListener("click", () => this.revert());
    void this.load();
  }

  async load(): Promise<void> {
    const response = await fetch("/api/config");
    if (!response.ok) {
      return;
    }
    this.baseline = (await response.json()) as HermesProjectConfigView;
    this.draft = structuredClone(this.baseline);
    this.render();
  }

  private render(): void {
    if (!this.draft) {
      return;
    }
    const sections = this.container.querySelector("#config-sections")!;
    sections.innerHTML = "";
    sections.appendChild(this.renderSection("Game", this.draft.game.sourceFile, [
      ["title", "Title", this.draft.game.title, "text"],
      ["scene", "Scene", this.draft.game.scene, "text"],
    ], (key, value) => {
      this.draft!.game = { ...this.draft!.game, [key]: value };
      this.markDirty();
    }));
    sections.appendChild(
      this.renderSection("Project", this.draft.project.sourceFile, [
        ["applicationClass", "Application class", this.draft.project.applicationClass, "text"],
        ["assetsDirectory", "Assets directory", this.draft.project.assetsDirectory, "text"],
        ["debug", "Debug", String(this.draft.project.debug), "checkbox"],
        ["version", "Version", this.draft.project.version, "text"],
      ], (key, value) => {
        const project = { ...this.draft!.project };
        if (key === "debug") {
          project.debug = value === "true";
        } else {
          (project as Record<string, string | boolean>)[key] = value;
        }
        this.draft!.project = project;
        this.markDirty();
      }),
    );
    sections.appendChild(this.renderPlatformsSection());
    this.updateDirtyIndicator();
  }

  private renderPlatformsSection(): HTMLElement {
    const section = document.createElement("details");
    section.className = "config-section";
    section.open = true;
    const summary = document.createElement("summary");
    summary.textContent = `Platforms (${this.draft!.platforms.sourceFile})`;
    section.appendChild(summary);
    const body = document.createElement("div");
    body.className = "section-body";
    for (const name of ["desktop", "html", "android"] as const) {
      const entry = this.draft!.platforms[name];
      body.appendChild(this.platformFields(name, entry));
    }
    section.appendChild(body);
    return section;
  }

  private platformFields(name: string, entry: PlatformEntry): HTMLElement {
    const block = document.createElement("div");
    block.innerHTML = `<strong>${name}</strong>`;
    block.appendChild(
      this.fieldRow("enabled", "Enabled", String(entry.enabled), "checkbox", (value) => {
        this.draft!.platforms = {
          ...this.draft!.platforms,
          [name]: { ...entry, enabled: value === "true" },
        };
        this.markDirty();
      }),
    );
    if (entry.width != null) {
      block.appendChild(
        this.fieldRow("width", "Width", String(entry.width), "number", (value) => {
          this.draft!.platforms = {
            ...this.draft!.platforms,
            [name]: { ...this.draft!.platforms[name as "desktop" | "html" | "android"], width: Number(value) },
          };
          this.markDirty();
        }),
      );
    }
    if (entry.height != null) {
      block.appendChild(
        this.fieldRow("height", "Height", String(entry.height), "number", (value) => {
          this.draft!.platforms = {
            ...this.draft!.platforms,
            [name]: { ...this.draft!.platforms[name as "desktop" | "html" | "android"], height: Number(value) },
          };
          this.markDirty();
        }),
      );
    }
    return block;
  }

  private renderSection(
    title: string,
    sourceFile: string,
    fields: [string, string, string, string][],
    onChange: (key: string, value: string) => void,
  ): HTMLElement {
    const section = document.createElement("details");
    section.className = "config-section";
    section.open = true;
    const summary = document.createElement("summary");
    summary.textContent = `${title} (${sourceFile})`;
    section.appendChild(summary);
    const body = document.createElement("div");
    body.className = "section-body";
    for (const [key, label, value, type] of fields) {
      body.appendChild(this.fieldRow(key, label, value, type, (v) => onChange(key, v)));
    }
    section.appendChild(body);
    return section;
  }

  private fieldRow(
    key: string,
    label: string,
    value: string,
    type: string,
    onChange: (value: string) => void,
  ): HTMLElement {
    const row = document.createElement("div");
    row.className = "field";
    const labelEl = document.createElement("label");
    labelEl.textContent = label;
    labelEl.title = key;
    row.appendChild(labelEl);
    const input = document.createElement("input");
    input.type = type === "checkbox" ? "checkbox" : type;
    if (type === "checkbox") {
      input.checked = value === "true";
    } else {
      input.value = value;
    }
    input.addEventListener("input", () => {
      onChange(type === "checkbox" ? String((input as HTMLInputElement).checked) : input.value);
    });
    row.appendChild(input);
    return row;
  }

  private markDirty(): void {
    this.updateDirtyIndicator();
  }

  private updateDirtyIndicator(): void {
    const dirty = JSON.stringify(this.baseline) !== JSON.stringify(this.draft);
    this.dirtyIndicator.textContent = dirty ? "Unsaved changes" : "";
  }

  private revert(): void {
    if (!this.baseline) {
      return;
    }
    this.draft = structuredClone(this.baseline);
    this.render();
  }

  private async save(): Promise<void> {
    if (!this.draft) {
      return;
    }
    const response = await fetch("/api/config", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(this.draft),
    });
    if (response.ok) {
      this.baseline = structuredClone(this.draft);
      this.updateDirtyIndicator();
    }
  }
}
