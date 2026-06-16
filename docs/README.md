# Hermes documentation

| Document                                   | Description                                                                                      |
|--------------------------------------------|--------------------------------------------------------------------------------------------------|
| [scene-format-v1.md](scene-format-v1.md)   | JSON scene format v1: entities, built-in components, Material/Mesh, entity types, `"ui"`, `UiAttach` |
| [ui-format-v1.md](ui-format-v1.md)         | UI document v1: widget trees, layout anchors, bindings, actions, author tiers 0–4                  |
| [render-pipeline.md](render-pipeline.md)   | JSON render pipeline: passes, FBOs, `ui` pass, shaders                                               |
| [entity-types.md](entity-types.md)         | Reusable `type.json` templates, merge rules, `$ref`, spawn, WorldManager                           |
| [scene-management.md](scene-management.md) | Scene stack, `SceneChangeRequest`, `HermesSession`, `SystemScope`, registration                  |
| [input.md](input.md)                       | `InputService`, actions vs devices, UI button actions, cookbook                                  |
| [coordinate-spaces.md](coordinate-spaces.md) | SCREEN / SURFACE / WORLD / NORMALIZED and `ViewportService`                                    |
| [input-format-v1.md](input-format-v1.md)   | Input profile JSON v1: actions, bindings, context, keyboard/pointer/gamepad                      |
| [runtime-config.md](runtime-config.md)     | Unified runtime config: Gradle DSL, `hermes-runtime.properties`, logging, platform alignment       |
| [resource-management.md](resource-management.md) | Central resource loading: bundles, catalog, async preload, loading screen, HTML cooperative async |
| [ARCHITECTURE.md](ARCHITECTURE.md)         | Module graph, libGDX boundary, `HERMES_HOME` / Maven local, `.hermes/platforms/`, tooling layout |
| [CONTRIBUTING.md](CONTRIBUTING.md)         | JDK 17, Gradle test commands, doctor/export smoke, dogfood desktop run                           |

Repository onboarding and phase guides: [../README.md](../README.md).
