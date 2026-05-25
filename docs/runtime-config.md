# Runtime configuration

Hermes resolves launch and runtime settings from a **single canonical pipeline** so desktop, HTML, Android, and export builds behave the same.

## Configuration layers (priority high → low)

1. JVM `-Dhermes.*` overrides (CI, local debugging)
2. Packaged `hermes-runtime.properties` (generated at build time)
3. Gradle `hermes { }` DSL (`debug`, `logging`, `platforms`, `runtime`)
4. `hermes.json` (game title, default scene, render pipeline)

## Gradle DSL (no-code path)

```groovy
hermes {
    applicationClass = 'com.example.MyGame'
    debug = true

    logging {
        minLevel = 'INFO'          // DEBUG | INFO | WARN | ERROR
        patternType = 'WILDCARD'   // WILDCARD | REGEX
        patterns = ['hermes.scene.*', '*Render*']
    }

    runtime {
        put 'custom.difficulty', 'normal'
    }

    platforms {
        desktop { width = 1280; height = 720 }
        html    { devServerPort = 8080 }
    }
}
```

### Logging defaults

| Condition | Default `hermes.log.minLevel` |
|-----------|-------------------------------|
| `logging.minLevel` set | That value |
| Distribution export | `WARN` |
| `debug = true` | `DEBUG` |
| `debug = false` | `INFO` |

## Generated runtime file

`:game:generateHermesRuntimeConfig` writes:

```
game/build/generated/hermes-runtime/hermes-runtime.properties
```

- **Desktop:** on `:game` classpath via `processResources`
- **HTML:** bundled by `TeaVMBuilder` from `hermes.runtime.config.dir`
- **Android:** packaged via `:game` resources (launcher `preBuild` depends on generation)

### HTML asset layout

TeaVM bundles the generated directory as libGDX internal assets. Runtime code loads
`hermes-runtime.properties` via `HermesAssetPaths.internal(...)`, which resolves both:

- `hermes-runtime.properties` (desktop classpath / JAR root)
- `assets/hermes-runtime.properties` (HTML webapp — actual TeaVM output path)

## Programmatic overrides (complex games)

```java
@Override
public void configureRuntime(RuntimeConfigBuilder config) {
    config.put("custom.seed", Long.toString(System.nanoTime()));
}

@Override
public void onCreate(HermesEngine engine) {
    String difficulty = engine.runtimeConfig().get("custom.difficulty", "normal");
}
```

## JVM overrides

```bash
./gradlew :game:hermesRunDesktop -Dhermes.log.minLevel=ERROR
./gradlew :game:hermesRunHtml   -Dhermes.log.patterns='*Scene*'
```

## Adding a new config key (engine developers)

1. Add constant to `RuntimeConfigKeys` (hermes-tooling)
2. Add Gradle DSL field if user-facing
3. Merge in `LaunchConfigResolver` (one builder call)
4. Add typed accessor on `RuntimeConfigService` if read at runtime
5. Document here
6. Add resolver unit test

Platform launchers should **not** assemble keys individually — they bundle the canonical file.

## Architecture

Build time: `LaunchConfigResolver` → `hermes-runtime.properties`  
Run time: `HermesRuntimeConfig` → `RuntimeConfigService` → logging, scenes, launchers

See also: [ARCHITECTURE.md](ARCHITECTURE.md)
