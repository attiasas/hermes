import type { EntitySnapshot, FieldSnapshot, HdpClient } from "./hdp";

function debounce(
  fn: (entityId: string, componentType: string, field: string, value: unknown) => void,
  ms: number,
): typeof fn {
  let timer: ReturnType<typeof setTimeout> | undefined;
  return (entityId, componentType, field, value) => {
    if (timer) {
      clearTimeout(timer);
    }
    timer = setTimeout(() => fn(entityId, componentType, field, value), ms);
  };
}

export class InspectorPanel {
  private entity: EntitySnapshot | null = null;
  private readonly debouncedSetField: ReturnType<typeof debounce>;

  constructor(
    private readonly container: HTMLElement,
    private readonly hdp: HdpClient,
  ) {
    this.debouncedSetField = debounce(
      (
        entityId: string,
        componentType: string,
        field: string,
        value: unknown,
      ) => {
        this.hdp.setComponentField(entityId, componentType, field, value);
      },
      200,
    );
  }

  showEntity(entity: EntitySnapshot | null): void {
    this.entity = entity;
    this.container.innerHTML = "";
    if (!entity) {
      this.container.textContent = "Select an entity";
      return;
    }
    for (const component of entity.components) {
      const section = document.createElement("section");
      const title = document.createElement("h3");
      title.textContent = component.type;
      section.appendChild(title);
      for (const field of component.fields) {
        section.appendChild(this.renderField(entity.id, component.type, field));
      }
      this.container.appendChild(section);
    }
  }

  private renderField(entityId: string, componentType: string, field: FieldSnapshot): HTMLElement {
    const wrapper = document.createElement("div");
    wrapper.className = "field";
    const label = document.createElement("label");
    label.textContent = field.name;
    wrapper.appendChild(label);

    const input = this.createInput(field);
    input.disabled = !field.editable;
    input.addEventListener("input", () => {
      const value = this.readValue(field, input);
      this.debouncedSetField(entityId, componentType, field.name, value);
    });
    wrapper.appendChild(input);
    return wrapper;
  }

  private createInput(field: FieldSnapshot): HTMLInputElement | HTMLSelectElement {
    if (field.kind === "BOOLEAN") {
      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.checked = Boolean(field.value);
      return checkbox;
    }
    if (field.kind === "ENUM" && Array.isArray((field as FieldSnapshot & { enumValues?: string[] }).enumValues)) {
      const select = document.createElement("select");
      for (const option of (field as FieldSnapshot & { enumValues: string[] }).enumValues) {
        const opt = document.createElement("option");
        opt.value = option;
        opt.textContent = option;
        opt.selected = option === String(field.value);
        select.appendChild(opt);
      }
      return select;
    }
    const input = document.createElement("input");
    input.type = field.kind === "STRING" ? "text" : "number";
    input.value = String(field.value ?? "");
    return input;
  }

  private readValue(field: FieldSnapshot, input: HTMLInputElement | HTMLSelectElement): unknown {
    if (field.kind === "BOOLEAN" && input instanceof HTMLInputElement) {
      return input.checked;
    }
    if (field.kind === "INT") {
      return parseInt(input.value, 10);
    }
    if (field.kind === "FLOAT") {
      return parseFloat(input.value);
    }
    return input.value;
  }
}
