export type FileNode = { path: string; directory: boolean };

export class FilesPanel {
  constructor(private readonly list: HTMLUListElement) {
    this.list.addEventListener("dblclick", (event) => {
      const target = (event.target as HTMLElement).closest("li[data-path]");
      if (!target) {
        return;
      }
      const path = target.getAttribute("data-path");
      if (!path || target.getAttribute("data-directory") === "true") {
        return;
      }
      void fetch("/api/files/open", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ path }),
      });
    });
  }

  async refresh(): Promise<void> {
    const response = await fetch("/api/files/tree");
    if (!response.ok) {
      return;
    }
    const nodes = (await response.json()) as FileNode[];
    this.list.innerHTML = "";
    for (const node of nodes) {
      const item = document.createElement("li");
      item.dataset.path = node.path;
      item.dataset.directory = String(node.directory);
      item.textContent = (node.directory ? "📁 " : "📄 ") + node.path;
      this.list.appendChild(item);
    }
  }
}
