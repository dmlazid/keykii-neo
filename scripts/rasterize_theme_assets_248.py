#!/usr/bin/env python3
"""Verify active KeyKii theme motifs and font assets (30 themes + 6 new fonts).

Note: Bulk illustrated packs (120-139) and the 512 Neo packs (1000-1511) were
removed, so this script validates the active 20 original themes (100-119),
10 clean gradient themes (200-209), their vector motif drawables, and the
6 new bundled TTF fonts (152-157) without regenerating deleted bulk art.
"""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
FONTS_DIR = ROOT / "app" / "src" / "main" / "assets" / "fonts"
DRAWABLE_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable"

REQUIRED_NEW_FONTS = (
    "quicksand.ttf",
    "playfair_display.ttf",
    "space_grotesk.ttf",
    "righteous.ttf",
    "dancing_script.ttf",
    "comfortaa.ttf",
)

REQUIRED_MOTIFS = (
    "theme_motif_bear.xml",
    "theme_motif_bow.xml",
    "theme_motif_bubble.xml",
    "theme_motif_bunny.xml",
    "theme_motif_butterfly.xml",
    "theme_motif_cake.xml",
    "theme_motif_cat.xml",
    "theme_motif_cherry.xml",
    "theme_motif_cloud.xml",
    "theme_motif_cup.xml",
    "theme_motif_flower.xml",
    "theme_motif_frog.xml",
    "theme_motif_gift.xml",
    "theme_motif_heart.xml",
    "theme_motif_leaf.xml",
    "theme_motif_lotus.xml",
    "theme_motif_moonstar.xml",
    "theme_motif_snow.xml",
    "theme_motif_star.xml",
    "theme_motif_strawberry.xml",
)

VALID_SFNT_HEADERS = (b"\x00\x01\x00\x00", b"OTTO", b"true")


def main() -> int:
    errors = []

    for font_name in REQUIRED_NEW_FONTS:
        font_path = FONTS_DIR / font_name
        if not font_path.is_file():
            errors.append(f"Missing new font asset: {font_path}")
            continue
        data = font_path.read_bytes()
        if len(data) < 10_000:
            errors.append(f"Font asset too small ({len(data)} bytes): {font_name}")
        if data[:4] not in VALID_SFNT_HEADERS:
            errors.append(f"Invalid font SFNT header in {font_name}: {data[:4]!r}")

    for motif_name in REQUIRED_MOTIFS:
        motif_path = DRAWABLE_DIR / motif_name
        if not motif_path.is_file():
            errors.append(f"Missing motif vector drawable: {motif_path}")
            continue
        try:
            ET.fromstring(motif_path.read_text(encoding="utf-8"))
        except ET.ParseError as exc:
            errors.append(f"Invalid XML in {motif_name}: {exc}")

    if errors:
        for err in errors:
            print(f"ERROR: {err}", file=sys.stderr)
        return 1

    print(
        f"Verified {len(REQUIRED_MOTIFS)} theme motif drawables and "
        f"{len(REQUIRED_NEW_FONTS)} new font assets for the 30 active KeyKii themes."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
