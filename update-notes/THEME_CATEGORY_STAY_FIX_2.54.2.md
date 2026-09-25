# KeyKii Neo 2.54.2 — Theme category stay fix

## Fixed
- Theme category selection now uses the current in-memory mode during redraw instead of re-reading and resetting it.
- Saved Theme category is loaded once when SettingsActivity starts.
- Theme filter bar automatically scrolls to the active category after redraw, so Dark/Cute/Aesthetic/etc. stays visible.
- Font filter uses the same persistence behavior to prevent the same reset issue later.
- Existing language keyboard-popup fix from 2.54.1 is preserved.

This update is focused only on category persistence/visibility.
