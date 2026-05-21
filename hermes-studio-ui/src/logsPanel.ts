export class LogsPanel {
  private readonly maxLines = 500;

  constructor(private readonly pre: HTMLPreElement) {}

  append(line: string): void {
    const text = this.pre.textContent ?? "";
    const lines = (text + line + "\n").split("\n");
    if (lines.length > this.maxLines) {
      this.pre.textContent = lines.slice(lines.length - this.maxLines).join("\n");
    } else {
      this.pre.textContent = text + line + "\n";
    }
    this.pre.scrollTop = this.pre.scrollHeight;
  }

  clear(): void {
    this.pre.textContent = "";
  }
}
