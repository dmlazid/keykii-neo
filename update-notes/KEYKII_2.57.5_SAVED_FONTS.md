# KeyKii Neo 2.57.5 — Saved fonts + complete Mine collection

## New
- Font previews now have **♡ Save / ♥ Saved**.
- Saved fonts appear in **Fonts > Saved**.
- Applying a font automatically records it under **Recently used fonts**.
- Mine now shows:
  - current theme and font
  - saved theme count
  - saved font count
  - favorite themes
  - recent themes
  - saved fonts
  - recent fonts
  - 24-hour access state and keyboard test box
- Saved/recent font data is local-only and works without an account.

## Release organization
- All future generated download files are committed under:
  `release-files/<version>/`
- Each version folder contains:
  - APK
  - Play AAB
  - SHA-256 checksum file
  - renderer audit ZIP
  - README
- The existing root APK is retained for backward-compatible links.
- Fixed the GitHub Actions release-folder YAML so packaging can run normally.

## Version
- versionName: **2.57.5**
- versionCode: **167**
