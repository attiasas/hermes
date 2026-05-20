#!/usr/bin/env bash
# Validates Hermes export ZIPs by extracting and checking for platform-specific artifacts.
# Usage: validate-export-zip.sh <platform> <path-to.zip>
set -euo pipefail

PLATFORM="${1:?platform required (html|android|desktop-linux|desktop-macos|desktop-windows)}"
ZIP="${2:?zip path required}"

if [[ ! -f "$ZIP" ]]; then
  echo "ZIP not found: $ZIP" >&2
  exit 1
fi

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

unzip -q "$ZIP" -d "$TMP"

case "$PLATFORM" in
  html)
    test -f "$TMP/webapp/index.html"
    if [[ -f "$TMP/webapp/app.wasm" ]]; then
      echo "OK: webapp/index.html and webapp/app.wasm"
    elif [[ -f "$TMP/webapp/app.js" ]]; then
      echo "OK: webapp/index.html and webapp/app.js"
    else
      echo "Missing webapp/app.wasm or webapp/app.js in $ZIP" >&2
      exit 1
    fi
    ;;
  android)
    APK_COUNT="$(find "$TMP" -name '*.apk' | wc -l | tr -d ' ')"
    if [[ "$APK_COUNT" -lt 1 ]]; then
      echo "No .apk found in $ZIP" >&2
      exit 1
    fi
    echo "OK: found $APK_COUNT APK(s)"
    ;;
  desktop-linux)
  BIN="$(find "$TMP" -type f ! -name '*.jar' ! -name '*.so' ! -path '*/runtime/*' ! -path '*/legal/*' ! -path '*/conf/*' \
    \( -perm -111 -o -name '*.bin' \) 2>/dev/null | head -1)"
    if [[ -z "$BIN" ]]; then
      BIN="$(find "$TMP" -maxdepth 3 -type f ! -name '*.jar' ! -name '*.png' ! -name '*.json' ! -name '*.txt' \
        ! -path '*/runtime/*' 2>/dev/null | head -1)"
    fi
    if [[ -z "$BIN" ]]; then
      echo "No launcher binary found in $ZIP" >&2
      find "$TMP" -type f | head -20 >&2
      exit 1
    fi
    echo "OK: desktop linux bundle contains $(basename "$BIN")"
    ;;
  desktop-macos)
    APP="$(find "$TMP" -name '*.app' -type d | head -1)"
    if [[ -z "$APP" ]]; then
      echo "No .app bundle found in $ZIP" >&2
      exit 1
    fi
    MACOS_DIR="$APP/Contents/MacOS"
    BIN=""
    for candidate in "$MACOS_DIR"/*; do
      [[ -f "$candidate" ]] || continue
      base="$(basename "$candidate")"
      [[ "$base" == *.jar ]] && continue
      [[ "$base" == "runtime" ]] && continue
      BIN="$candidate"
      break
    done
    if [[ -z "$BIN" ]]; then
      echo "No launcher binary under $MACOS_DIR" >&2
      exit 1
    fi
    echo "OK: $(basename "$APP") with launcher $(basename "$BIN")"
    ;;
  desktop-windows)
    if ! find "$TMP" -name '*.exe' -type f | grep -q .; then
      echo "No .exe found in $ZIP" >&2
      exit 1
    fi
    echo "OK: windows bundle contains .exe"
    ;;
  *)
    echo "Unknown platform: $PLATFORM" >&2
    exit 1
    ;;
esac
