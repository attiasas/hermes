# Hermes Debug Protocol (HDP) v0

HDP is a **localhost-only** WebSocket protocol between a running Hermes game (when `debug` is enabled) and tools such as Hermes Studio or the VS Code inspector webview.

## Transport

| Property | Value |
|----------|-------|
| URL | `ws://127.0.0.1:<port>` |
| Default port | `18765` (`-Dhermes.debug.port=18765` on `hermesRunDesktop`) |
| Binding | `127.0.0.1` only — not exposed on the LAN |
| Encoding | UTF-8 JSON text frames |

The debug server starts only when `hermes.debug=true` (Gradle `debug = true` on `:game`). Export and release builds use `hermes.debug=false`; the listener is not started.

## Server → client messages

The server pushes two message types about every **150 ms**, and again immediately when a client connects.

### `worldSnapshot`

```json
{
  "type": "worldSnapshot",
  "worldSnapshot": {
    "frame": 42,
    "scenePath": "scenes/main.json",
    "entities": [
      {
        "id": "logo",
        "name": "logo",
        "components": [
          {
            "type": "Transform",
            "properties": { "x": 140.0, "y": 210.0 },
            "fields": [
              { "name": "x", "kind": "FLOAT", "editable": true, "value": 140.0 },
              { "name": "y", "kind": "FLOAT", "editable": true, "value": 210.0 }
            ]
          }
        ]
      }
    ]
  }
}
```

### `stats`

```json
{
  "type": "stats",
  "stats": {
    "fps": 60.0,
    "entityCount": 3
  }
}
```

### `error`

```json
{
  "type": "error",
  "error": "setComponentField requires entityId, componentType, and field"
}
```

## Client → server commands

Send a single JSON object per command. All mutations run on the libGDX thread via `Gdx.app.postRunnable`.

| `type` | Fields | Effect |
|--------|--------|--------|
| `pause` | — | Pause simulation |
| `resume` | — | Resume simulation |
| `reloadScene` | — | Reload the active scene |
| `setComponentField` | `entityId`, `componentType`, `field`, `value` | Apply one inspector field |

### `setComponentField` schema

```json
{
  "type": "setComponentField",
  "entityId": "logo",
  "componentType": "Transform",
  "field": "x",
  "value": 200.0
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `entityId` | string | yes | Entity id from the snapshot |
| `componentType` | string | yes | Registered component type name |
| `field` | string | yes | Inspector field name from `FieldDescriptor` |
| `value` | number, boolean, string, or null | no | Omitted or null clears where supported |

The server validates the entity and field, then delegates to `ComponentRegistryImpl.applyField`. Unknown fields or types produce an `error` message.

## Built-in vs custom components

Built-in components (`Transform`, `Sprite`, `Camera`, …) ship with serializers and inspector descriptors in `hermes-core`. Custom components register `ComponentSerializer` and `ComponentInspectorDescriptor` — see [component-inspector-registration.md](component-inspector-registration.md).

## Implementation references

- Server: `hermes-core/.../HermesDebugServer.java`
- DTOs: `hermes-debug-api/.../HdpMessage.java`, `WorldSnapshot.java`
- UI client: `hermes-studio-ui/src/hdp.ts`
