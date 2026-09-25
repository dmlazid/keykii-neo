# KeyKii Neo 2.54.3 — Instant Theme Filters

## Fixed
- Theme Shop always opens on All when the app starts. It no longer reopens directly on Dark or another old category.
- Theme category taps no longer rebuild the entire Theme Shop screen.
- Only the theme-results container is replaced, so category changes are much faster.
- The active category chip stays visible and selected.
- Fixed the incorrect category chip tag that caused the horizontal bar to jump back to the left.
- Added a short fade transition when changing categories.

## Expected behavior
1. Open KeyKii -> Theme Shop starts at All.
2. Tap Dark -> Dark remains selected and Dark themes stay visible.
3. Tap Cute/Aesthetic/etc. -> that category remains selected.
4. Switch to another app section and return during the same session -> current category remains.
5. Close/reopen KeyKii -> Theme Shop starts at All again.

No theme artwork, font library, keyboard height, translator, cursor, emoji or typing behavior was changed.
