# Component inspector registration

Hermes Studio and HDP show editable fields only for components that register a **serializer** (scene + live apply) and an **inspector descriptor** (field list for the UI).

## ServiceLoader path (recommended for templates)

Implement `dev.hermes.api.ecs.ComponentRegistration` and register it under:

`META-INF/services/dev.hermes.api.ecs.ComponentRegistration`

## Example: `SpinMarker`

The sample game registers `SpinMarker` via SPI in [`SpinMarkerRegistration.java`](../game/src/main/java/dev/hermes/sample/SpinMarkerRegistration.java).

### 1. Component type

```java
public final class SpinMarker implements Component {
  // speedRadiansPerSecond, centerX, centerY, radius, angleRadians …
}
```

### 2. Registration entry point

```java
public final class SpinMarkerRegistration implements ComponentRegistration {
  @Override
  public void register(HermesEngine engine) {
    engine.registry().register(
        "SpinMarker",
        SpinMarker.class,
        data -> { /* deserialize from scene JSON */ },
        spinMarkerSerializer(),
        spinMarkerDescriptor());
    engine.addSystem(new SpinMarkerSystem());
  }
}
```

### 3. Serializer (scene + live edits)

`ComponentSerializer` provides:

- `serialize` — write current values into `MutableComponentData` for snapshots
- `applyField` — handle `setComponentField` from HDP for each editable property

```java
@Override
public void applyField(Component component, String fieldName, Object value) {
  SpinMarker spin = (SpinMarker) component;
  switch (fieldName) {
    case "speedRadiansPerSecond" -> spin.setSpeedRadiansPerSecond(asFloat(value));
    case "centerX" -> spin.setCenterX(asFloat(value));
    // …
    default -> throw new IllegalArgumentException("Unknown SpinMarker field: " + fieldName);
  }
}
```

### 4. Inspector descriptor

`ComponentInspectorDescriptor` lists fields the Studio inspector may edit:

```java
return () -> List.of(
    new FieldDescriptor("speedRadiansPerSecond", FieldKind.FLOAT, true),
    new FieldDescriptor("centerX", FieldKind.FLOAT, true),
    new FieldDescriptor("centerY", FieldKind.FLOAT, true),
    new FieldDescriptor("radius", FieldKind.FLOAT, true),
    new FieldDescriptor("angleRadians", FieldKind.FLOAT, false));
```

`editable: false` fields appear read-only (e.g. runtime `angleRadians` updated by `SpinMarkerSystem`).

### 5. Scene JSON

Scene entities can reference the type by name:

```json
{
  "id": "spinner",
  "components": {
    "SpinMarker": {
      "speedRadiansPerSecond": 1.5,
      "centerX": 320,
      "centerY": 240,
      "radius": 100
    }
  }
}
```

### 6. Verify in Studio

1. Set `debug = true` in `game/build.gradle`.
2. Run `./gradlew :game:hermesRunDesktop` or Studio **Play**.
3. Open Studio or **Hermes: Open Inspector** in VS Code.
4. Select an entity with `SpinMarker`; edit `centerX` / `radius` and confirm motion updates live.

## Explicit registration (`onCreate`)

Alternatively call `engine.registry().register(...)` from `HermesGame.onCreate` without SPI — same serializer and descriptor types. The empty template uses SPI for `PulseMarker`; the engine sample uses SPI for `SpinMarker` and explicit registration for `BounceMarker`.

## Field kinds

| `FieldKind` | JSON value type |
|-------------|-----------------|
| `FLOAT` | number |
| `INT` | number |
| `BOOLEAN` | boolean |
| `STRING` | string |
| `ENUM` | string (allowed values from descriptor when used) |

See [hermes-debug-protocol-v0.md](hermes-debug-protocol-v0.md) for the wire format of `setComponentField`.
