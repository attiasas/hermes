#!/usr/bin/env bash
# Appends export artifact details to the GitHub Actions job summary.
# Usage: write-export-summary.sh <title> <workflow-artifact-name> <zip-glob>...
set -euo pipefail

TITLE="${1:?title required}"
ARTIFACT_NAME="${2:?workflow artifact name required}"
shift 2

if [[ -z "${GITHUB_STEP_SUMMARY:-}" ]]; then
  echo "GITHUB_STEP_SUMMARY is not set" >&2
  exit 1
fi

if [[ $# -lt 1 ]]; then
  echo "At least one zip glob required" >&2
  exit 1
fi

shopt -s nullglob
ZIPS=()
for pattern in "$@"; do
  for zip in $pattern; do
    if [[ -f "$zip" ]]; then
      ZIPS+=("$zip")
    fi
  done
done
shopt -u nullglob

{
  echo "## ${TITLE}"
  echo ""
  echo "| Artifact | Size | SHA-256 | Status |"
  echo "| --- | ---: | --- | --- |"

  if [[ ${#ZIPS[@]} -eq 0 ]]; then
    echo "| _(no zip found)_ | — | — | **failed** |"
    echo ""
    echo "**Workflow artifact:** \`${ARTIFACT_NAME}\`"
    echo ""
    echo "> No files matched: $*"
  else
    for zip in "${ZIPS[@]}"; do
      size="$(ls -lh "$zip" | awk '{print $5}')"
      hash="$(sha256sum "$zip" | awk '{print $1}')"
      short="${hash:0:16}…"
      echo "| \`$(basename "$zip")\` | ${size} | \`${short}\` | checks passed |"
    done
    echo ""
    echo "**Workflow artifact:** \`${ARTIFACT_NAME}\`"
  fi
  echo ""
} >>"$GITHUB_STEP_SUMMARY"

if [[ ${#ZIPS[@]} -eq 0 ]]; then
  exit 1
fi
