# Game icons

Replace these files to customize app icons for export. Paths are relative to `assetsDirectory` (`src/main/resources/assets` by default).

| File | Platform |
|------|----------|
| `desktop/mac.icns` | macOS `.app` (Construo) |
| `desktop/windows.png` | Windows `.exe` |
| `android/ic_launcher.png` | Android launcher (512×512 PNG recommended) |
| `web/favicon.png` | HTML export favicon |

Re-run `./gradlew :game:hermesExport*` after changing icons.
