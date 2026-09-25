# KeyKii Neo 2.51.0 — 1,000+ Font Expansion

This release expands the Font Shop without changing keyboard height, bottom spacing,
themes, translator, cursor control, emoji, kaomoji, or typed text output.

## Font library
- 1,083 total keyboard font styles
- 1,052 new styles beyond the previous 31-font build
- 28 new bundled featured fonts for instant offline preview
- 1,024 additional open-source Google Fonts available on demand
- 320 on-demand fonts labeled FREE / rewarded-ad
- 704 on-demand fonts labeled PRO
- existing System, Rounded, Serif, Mono and all previous fonts preserved

## Behavior
- Font selection changes only KeyKii keyboard letters/numbers and suggestion text.
- Text committed into apps remains normal Unicode text.
- Theme selection never overwrites the chosen font.
- On-demand fonts are cached in app storage after preview/download.
- Font cards update to the real downloaded typeface, rather than a fake generated preview.
- The 1,024-font catalog loads 48 rows at a time to keep the settings screen responsive.

## Licensing
The on-demand catalog uses only SIL Open Font License families from the Google Fonts
repository, pinned to revision 23e54b51ddffbc7713c583748e3bd86f62b1fa4a. The OFL 1.1 license text is already
bundled in app/src/main/assets/fonts/licenses/OFL-1.1.txt.

## Monetization staging
FREE/AD and PRO classifications are present in the Font Shop. This development build
keeps direct apply enabled so the entire library can be tested before rewarded-ad and
Google Play Billing credentials are connected.
