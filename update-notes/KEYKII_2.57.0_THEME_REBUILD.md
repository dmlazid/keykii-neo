# KeyKii Neo 2.57.0 — visible theme diversity rebuild

## Why this version exists
2.56.0 still looked repetitive on the phone. The catalog had different IDs and geometry signatures, but category pages could still cluster the same scene/shape language and the concept-sheet designs were not wired into the actual APK strongly enough.

## What changed
- Version is now **2.57.0** / versionCode **161**.
- Added a third original scene atlas with 16 new source-controlled designs:
  - Galaxy Clouds
  - Felt Patch Garden
  - Kawaii Cats
  - Coffee Break
  - Pink Self Care
  - Sunset Tulips
  - Happy Cloud Sky
  - Painted Star Field
  - Pastel Brush Village
  - Cozy Study Desk
  - Retro Cute Tech
  - Chocolate Dream
  - Mosaic Ceramic
  - Ocean Shells
  - Botanical Notebook
  - City Neon Window
- The first 32 new themes are explicit curated recipes that correspond to the concept directions shown during review. They use 32 different structural architectures instead of a repeated generic board.
- Remaining recipes use a new permutation that spreads all 48 scenes across the catalog rather than repeatedly hitting only part of the artwork set.
- Theme category paging is now architecture-round-robin. The first page of every category is forced to show different structures before repeating an architecture.
- FREE and PRO continue to use exclusive architecture tiers, so the same structural family is not sold twice with only a small skin change.
- The live keyboard and Theme Shop preview still share the same renderer, artwork, geometry and spacebar treatment.

## New validation gates
The Android audit now checks:
- all three scene atlases decode;
- the 32 curated concept themes have unique names and unique structural architectures;
- exact visual recipes do not repeat;
- every category's first page contains at least eight different architectures when 8+ items are available;
- FREE and PRO architecture tiers do not cross;
- all 512 live keyboard layouts keep click/long-press handlers, keyboard height and non-overlapping touch bounds.

## Preserved
The 2.54.4 Theme/Font performance fixes, 24-card batching, append-only Show more, original 40 themes, keyboard height/bottom spacing, translator, grammar tools, cursor control, emoji/kaomoji, language system, intro, fonts and keyboard tools remain preserved.

All new artwork is original and based only on broad mood-board directions. No Pinterest screenshot, third-party character, logo or copied artwork is packaged in the APK.


## Build 162 — root-scene anti-repeat ordering
Phone testing showed that one category could still show Fruit Ice, Pumpkin Porch, Happy Cloud Sky, Coffee Break and other base themes repeatedly while scrolling. Different subtitles or architectures did not make that acceptable.

Build 162 changes the catalog ordering itself:
- a base scene is shown once before any second variant of that same scene;
- if a category has 24+ different base scenes, the first 24 cards are 24 different base scenes;
- later variants use a **12-card root-scene cooldown** whenever another unused scene is available;
- categories with fewer scene roots use the largest possible cooldown automatically;
- a shorter architecture cooldown also reduces repeated keyboard structures;
- ordering remains deterministic and cached, so 24-card lazy loading and append-only Show more stay fast.

The device audit now rejects unnecessary base-scene repeats on the first page and rejects any repeat inside the cooldown when a different remaining root could have been shown.

Version stays **2.57.0** with versionCode **162** so it installs over the previous 2.57.0 build.
