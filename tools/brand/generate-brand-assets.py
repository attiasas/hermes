#!/usr/bin/env python3
"""Generate Hermes-branded default assets for templates and plugin fallbacks.

Requires: pip install pillow

On macOS, also produces icons/desktop/mac.icns via iconutil when available.
"""

from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError:
    print("Install Pillow: pip install pillow", file=sys.stderr)
    sys.exit(1)

ROOT = Path(__file__).resolve().parents[2]
OUT_DIRS = [
    ROOT / "hermes-templates/minimal/game/src/main/resources/assets",
    ROOT / "hermes-templates/multi-scene/game/src/main/resources/assets",
    ROOT / "hermes-gradle-plugin/src/main/resources/hermes-default-icons",
]

# Hermes palette
BG = (15, 32, 48)  # deep navy
ACCENT = (0, 196, 180)  # teal
MARK = (240, 248, 255)  # off-white


def draw_mark(size: int) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    pad = size // 8
    radius = size // 5
    draw.rounded_rectangle(
        (pad, pad, size - pad, size - pad), radius=radius, fill=BG + (255,)
    )
    bar_w = max(size // 10, 4)
    x0 = size // 3
    x1 = size - size // 3
    y0 = size // 4
    y1 = size - size // 4
    ym = size // 2
    draw.rectangle((x0, y0, x0 + bar_w, y1), fill=MARK + (255,))
    draw.rectangle((x0, ym - bar_w // 2, x1, ym + bar_w // 2), fill=MARK + (255,))
    draw.rectangle((x1 - bar_w, y0, x1, y1), fill=MARK + (255,))
    # Teal accent dot
    r = size // 14
    draw.ellipse(
        (size - pad - r * 2, pad + r, size - pad, pad + r * 3),
        fill=ACCENT + (255,),
    )
    return img


def save_png(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if img.mode != "RGBA":
        img = img.convert("RGBA")
    img.save(path, "PNG")


def write_icns(png_512: Path, icns_path: Path) -> bool:
    if sys.platform != "darwin":
        print("  skip mac.icns (iconutil requires macOS)")
        return False
    iconutil = shutil.which("iconutil")
    if not iconutil:
        return False
    base = Image.open(png_512).convert("RGBA")
    # Apple iconset naming: icon_WxH.png and icon_WxH@2x.png (2x pixel dimensions)
    entries = [
        ("icon_16x16.png", 16),
        ("icon_16x16@2x.png", 32),
        ("icon_32x32.png", 32),
        ("icon_32x32@2x.png", 64),
        ("icon_128x128.png", 128),
        ("icon_128x128@2x.png", 256),
        ("icon_256x256.png", 256),
        ("icon_256x256@2x.png", 512),
        ("icon_512x512.png", 512),
        ("icon_512x512@2x.png", 1024),
    ]
    with tempfile.TemporaryDirectory() as tmp:
        iconset = Path(tmp) / "hermes.iconset"
        iconset.mkdir()
        for name, px in entries:
            save_png(base.resize((px, px), Image.Resampling.LANCZOS), iconset / name)
        result = subprocess.run(
            [iconutil, "-c", "icns", str(iconset), "-o", str(icns_path)],
            capture_output=True,
            text=True,
        )
        if result.returncode != 0:
            print(f"  iconutil failed: {result.stderr.strip()}", file=sys.stderr)
            return False
    return True


def copy_tree_assets(base_out: Path, mark: Image.Image) -> None:
    logo = mark.resize((256, 256), Image.Resampling.LANCZOS)
    launcher = mark.resize((512, 512), Image.Resampling.LANCZOS)
    favicon = mark.resize((64, 64), Image.Resampling.LANCZOS)
    windows = mark.resize((256, 256), Image.Resampling.LANCZOS)

    save_png(logo, base_out / "hermes-logo.png")
    save_png(launcher, base_out / "icons/android/ic_launcher.png")
    save_png(favicon, base_out / "icons/web/favicon.png")
    save_png(windows, base_out / "icons/desktop/windows.png")

    icns = base_out / "icons/desktop/mac.icns"
    if not write_icns(base_out / "icons/android/ic_launcher.png", icns):
        if icns.is_file():
            print(f"  kept existing {icns}")
        else:
            print(
                "  warning: could not create mac.icns (run on macOS with iconutil); "
                "PNG icons were still written",
                file=sys.stderr,
            )


def main() -> None:
    mark = draw_mark(512)
    for out in OUT_DIRS:
        print(f"Writing {out}")
        copy_tree_assets(out, mark)
    print("Done.")


if __name__ == "__main__":
    main()
