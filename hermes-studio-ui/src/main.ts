import { ConfigPanel } from "./configPanel";
import { FilesPanel } from "./filesPanel";
import { HdpClient } from "./hdp";
import { HierarchyPanel } from "./hierarchy";
import { InspectorPanel } from "./inspector";
import { LogsPanel } from "./logsPanel";
import { PanelHost } from "./panelHost";

async function fetchDebugPort(): Promise<number> {
  const response = await fetch("/api/play/port");
  if (!response.ok) {
    return 18765;
  }
  const body = (await response.json()) as { port: number };
  return body.port;
}

async function main(): Promise<void> {
  const status = document.querySelector("#status")!;
  const logs = new LogsPanel(document.querySelector("#logs")!);
  const hdpPort = await fetchDebugPort();
  const hdp = new HdpClient(hdpPort);

  const inspector = new InspectorPanel(document.querySelector("#inspector")!, hdp);
  const hierarchy = new HierarchyPanel(document.querySelector("#hierarchy")!, {
    onSelect: (entity) => inspector.showEntity(entity),
  });

  const configMount = document.querySelector("#config-panel-mount")!;
  const panelHost = new PanelHost();
  panelHost.registerPanel(new ConfigPanel());
  panelHost.mountAll(configMount as HTMLElement);

  const files = new FilesPanel(document.querySelector("#files-tree")!);
  await files.refresh();

  const connectHdp = () => {
    hdp.connect(
      (message) => {
        if (message.type === "worldSnapshot" && message.worldSnapshot) {
          hierarchy.update(message.worldSnapshot);
          const selected = hierarchy.selectedEntity();
          if (selected) {
            const updated = message.worldSnapshot.entities.find((e) => e.id === selected.id);
            inspector.showEntity(updated ?? selected);
          }
        }
        if (message.type === "error" && message.error) {
          logs.append(`HDP: ${message.error}`);
        }
      },
      (connected) => {
        status.textContent = connected ? `HDP :${hdpPort}` : "Disconnected";
      },
    );
  };

  document.querySelector("#btn-play")!.addEventListener("click", async () => {
    logs.append("Starting play...");
    const response = await fetch("/api/play/run", { method: "POST" });
    if (response.ok) {
      setTimeout(connectHdp, 2000);
    } else {
      logs.append("Play failed");
    }
  });

  document.querySelector("#btn-stop")!.addEventListener("click", async () => {
    await fetch("/api/play/stop", { method: "POST" });
    hdp.disconnect();
    status.textContent = "Disconnected";
    logs.append("Stopped");
  });

  connectHdp();
}

void main();
