# KeyKii Neo 2.47.1 — Actual Theme Renderer Fix

This build fixes the mismatch between attractive design references and the real applied keyboard.

## Rules
- Preserve packs 100–119.
- Rework only packs 120–139.
- The Theme Shop preview and live keyboard must load the same per-pack resources.
- No shared thick-white-outline key renderer for packs 120–139.
- Each new pack has its own board artwork, normal keycap, special keycap, and spacebar resource.
- Keep key labels readable and the keyboard geometry unchanged.
- Keep fonts separate.
- Keep More transparent as the default.
- New pack stickers are limited to Shift, Delete, and Space so layouts do not repeat tiny icons across Q/P/1/0/5.

## Acceptance
- Preview and applied keyboard use the same assets.
- New themes are visually distinct.
- Borders are thin/subtle, never thick bright white.
- Background composition is concentrated around edges and negative space instead of crossing the typing rows.
- Existing working keyboard features, height, and bottom spacing remain unchanged.
