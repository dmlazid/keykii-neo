# KeyKii Neo 2.54.1 — Language keyboard popup + category persistence fix

## Fixed
- Selecting an app language no longer opens the Android soft keyboard.
- Intro, language picker, Theme Shop, Font Shop, Mine and Settings explicitly hide the IME on entry.
- The language picker itself takes non-text focus so no text field can steal focus during language selection.
- Theme Shop category now stays selected after redraw/navigation/activity recreation.
- Theme category selection is persisted in `theme_browse_mode`.
- Font Shop filter selection is also persisted in `font_browse_mode` so it cannot reset unexpectedly.

## Preserved
- 2.54.0 opening intro and app language system.
- Theme categories: All, Free, Pro, Cute, Aesthetic, Dreamy, Dark, Nature, Seasonal.
- Existing themes, font shop, color fonts, keyboard height and typing tools.
