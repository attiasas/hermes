import type { EntitySnapshot, WorldSnapshot } from "./hdp";

export type HierarchyCallbacks = {
  onSelect: (entity: EntitySnapshot | null) => void;
};

export class HierarchyPanel {
  private readonly list: HTMLUListElement;
  private entities: EntitySnapshot[] = [];
  private selectedId: string | null = null;

  constructor(listElement: HTMLUListElement, private readonly callbacks: HierarchyCallbacks) {
    this.list = listElement;
    this.list.addEventListener("click", (event) => {
      const target = (event.target as HTMLElement).closest("li[data-entity-id]");
      if (!target) {
        return;
      }
      const id = target.getAttribute("data-entity-id");
      if (!id) {
        return;
      }
      this.select(id);
    });
  }

  update(snapshot: WorldSnapshot | undefined): void {
    this.entities = snapshot?.entities ?? [];
    this.list.innerHTML = "";
    for (const entity of this.entities) {
      const item = document.createElement("li");
      item.dataset.entityId = entity.id;
      item.textContent = entity.name || entity.id;
      if (entity.id === this.selectedId) {
        item.classList.add("selected");
      }
      this.list.appendChild(item);
    }
    if (this.selectedId && !this.entities.some((e) => e.id === this.selectedId)) {
      this.select(null);
    }
  }

  selectedEntity(): EntitySnapshot | null {
    if (!this.selectedId) {
      return null;
    }
    return this.entities.find((e) => e.id === this.selectedId) ?? null;
  }

  private select(id: string | null): void {
    this.selectedId = id;
    for (const item of this.list.querySelectorAll("li")) {
      item.classList.toggle("selected", item.getAttribute("data-entity-id") === id);
    }
    const entity = id ? (this.entities.find((e) => e.id === id) ?? null) : null;
    this.callbacks.onSelect(entity);
  }
}
