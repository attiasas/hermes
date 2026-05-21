export type FieldKind = "FLOAT" | "INT" | "BOOLEAN" | "STRING" | "ENUM";

export type FieldSnapshot = {
  name: string;
  kind: FieldKind;
  editable: boolean;
  value: unknown;
};

export type ComponentSnapshot = {
  type: string;
  properties: Record<string, unknown>;
  fields: FieldSnapshot[];
};

export type EntitySnapshot = {
  id: string;
  name: string;
  components: ComponentSnapshot[];
};

export type WorldSnapshot = {
  frame: number;
  scenePath: string;
  entities: EntitySnapshot[];
};

export type StatsFrame = {
  fps: number;
  entityCount: number;
};

export type HdpMessage = {
  type: string;
  worldSnapshot?: WorldSnapshot;
  stats?: StatsFrame;
  error?: string;
};

export class HdpClient {
  private socket: WebSocket | null = null;

  constructor(private readonly port: number) {}

  connect(onMessage: (message: HdpMessage) => void, onStatus?: (connected: boolean) => void): void {
    if (this.socket) {
      this.socket.close();
    }
    const url = `ws://127.0.0.1:${this.port}`;
    const socket = new WebSocket(url);
    this.socket = socket;
    socket.onopen = () => onStatus?.(true);
    socket.onclose = () => onStatus?.(false);
    socket.onerror = () => onStatus?.(false);
    socket.onmessage = (event) => {
      try {
        onMessage(JSON.parse(event.data as string) as HdpMessage);
      } catch {
        // Ignore malformed frames.
      }
    };
  }

  disconnect(): void {
    this.socket?.close();
    this.socket = null;
  }

  setComponentField(
    entityId: string,
    componentType: string,
    field: string,
    value: unknown,
  ): void {
    this.send({
      type: "setComponentField",
      entityId,
      componentType,
      field,
      value,
    });
  }

  pause(): void {
    this.send({ type: "pause" });
  }

  resume(): void {
    this.send({ type: "resume" });
  }

  private send(payload: Record<string, unknown>): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return;
    }
    this.socket.send(JSON.stringify(payload));
  }
}
