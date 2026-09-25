# KeyKii Neo 2.56.0 — real layout and artwork rebuild

## What the audit found
The earlier 2.56.0 implementation's 48 named "architectures" were background drawings. The service still positioned every key using its original LinearLayout rows. The catalog also contained a literal backslash-n that commented out the class declaration. A recent upstream fix corrected that compile error but did not add real layout variation.

This revision replaces that implementation. Version name stays **2.56.0**; version code increases to **159** so this APK can update the earlier 2.56.0 build.

## Real layouts and shared rendering
- `NeoGeometry` defines 48 authored structural profiles and 11 spatial arrangement editions. It calculates actual key view rectangles, row proportions, split gaps, column staggering, unequal key widths, modifier widths, and spacebar widths.
- `NeoKeyboardLayout` adopts the existing key views. It retains their original click, long-press, glide, backspace, language and spacebar-cursor listeners. Touch rectangles move with the visible keys.
- The adapted board uses the same total height as the original rows. Toolbar, prediction strip, bottom inset, keyboard resize settings and one-hand container are not replaced.
- `NeoThemeRenderer` supplies material, key silhouette, background and spacebar artwork for both the live keyboard and `NeoThemePreview`. Glass edges, picture caps, frosting, plush outlines, ticket cutouts, petals, irregular stickers, arches, mechanical bevels and round modifiers are drawn in key-relative coordinates.
- `NeoKeyboardLanguage` contains the preexisting row definitions, extracted without changing their letter order. Preview and IME share those definitions and respect the number-row setting.
- New previews fit the current keyboard aspect ratio without stretching key shapes.
- New previews use one Canvas view per card; the original 40 themes keep their existing preview and live renderer.

## Catalog counts — what the numbers mean
There are **512 theme compositions**, **48 geometry families**, **11 spatial arrangement editions**, and **32 newly generated original illustration scenes**. Artwork and renderer families are deliberately shared. This is not a claim of 512 separately illustrated artworks or 512 independently hand-drawn layouts. Geometry signatures differ for all 512 compositions, but that numerical test alone does not certify artistic uniqueness.

Each family's art brief limits scenes to coherent subjects. There is no HSV color-permutation loop. Editions change actual geometry and material treatment. The first 48 entries expose all 48 families before additional compositions appear.

The supplied references guided materials, mixed key shapes, dimensional keycaps, scene coverage, and decorated spacebars. No reference screenshot, competitor logo, or copyrighted character was embedded. The two original illustration atlases are in `app/src/main/assets/themes/v2/` and are sampled by the renderer, including panoramic key fills.

## Category performance
- Preserve the 2.54.4 immediate chip selection, no fade-to-white, progressive rows and generation-based cancellation.
- Retain batches of 24. "Show more" appends only the next batch instead of rebuilding all existing cards.
- Decode the two shared atlases once on a worker thread. Drawing never reads files or decodes images. Views repaint when assets become available.
- Catalog entries are cached; filtering allocates no new Entry objects.
- All requested categories are retained. Tags reflect the actual artwork rather than assigning Anime-inspired to non-anime backgrounds.
- All 25 identified font-related methods are unchanged from 2.54.4.

## Preserved features
The original theme IDs 100–139 and their assets are unchanged. Translator, grammar tools, cursor control, emoji/kaomoji, intro, dictionary, fonts, settings and language actions remain. Only the main keyboard's new-theme key rows are adopted by the new layout; other tool screens retain their original layouts.

## Validation gates
- Host JVM audit: 15,360 configurations across all 512 recipes, ten languages, number-row modes and symbols; bounds, key retention, non-overlap, minimum dimensions, one usable spacebar, and distinct geometry signatures.
- Android instrumentation: decode original artwork, capture all 48 families with the real Android preview, adapt all 512 live boards and check retained click/long-press handlers, key count, height, and non-overlapping actual view rectangles.
- The existing GitHub Actions build must compile the APK and test APK, pass native renderer audit, and then publish the APK. Audit images are uploaded separately for visual review.

## Limits to review on the phone
This is a renderer/artwork rebuild, not proof that every composition meets a subjective premium-art standard. Reused art scenes are documented above. A passing build and layout audit do not replace real typing, category responsiveness and visual approval on the user's device.
