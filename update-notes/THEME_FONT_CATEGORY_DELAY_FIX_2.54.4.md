# KeyKii Neo 2.54.4 — Theme + Font category delay fix

## What was actually wrong
The screenshots showed two separate UI problems:
- Theme category content faded out while expensive preview tiles were rebuilt, so the old category stayed visible as a white/faded screen for seconds.
- Font category taps rebuilt the entire Fonts screen, and the Font chips used the wrong tag prefix, so the active category could be off-screen or look unselected.

## Theme Shop fix
- Removed the fade-to-white category transition.
- Theme category chips update immediately.
- Filtered theme rows are now created progressively, one row at a time, so the UI remains responsive.
- Switching categories cancels unfinished rendering from the previous category.
- Entering Theme Shop always starts on All.
- All themes are rendered progressively too.

## Font Shop fix
- Font categories now update only the font-results container.
- The entire Fonts screen is no longer rebuilt when a category is tapped.
- Corrected Font filter tags.
- Active Font category automatically remains visible.
- Entering Font Shop starts on All.
- Removed persisted category state.
- Reduced first batch to 24 remote fonts / 30 color fonts for faster opening; all 1,383 styles remain available with Show more.
- Applying a font and Show more now update only the results container.

No theme, font, keyboard typing or renderer features were removed.
