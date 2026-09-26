# KeyKii Neo 2.57.9 — Private Owner Access

## New
- Added **Settings > Owner Access**.
- The KeyKii owner can enter a private one-time code and unlock permanent premium access on that device.
- Owner access includes all PRO themes and ad-gated fonts.
- Rewarded ads and random interstitial ads are disabled while Owner Access is active.
- The 24-hour expiry is ignored for the owner.
- The owner state is local to the device and survives normal app restarts/updates as long as app data is preserved.
- Owner Access can be disabled manually from the same screen.

## Security
- The plaintext owner code is NOT stored in the repository.
- Only a SHA-256 hash is compiled into the app.
- The owner code is long/random to make guessing impractical.
- Friends who install the same APK continue to use the normal monetization unless the owner shares the private code.

## Preserved
Sharing, Saved Looks, theme/font search, favorites, recents, renderer, original themes, Neo themes, keyboard tools and performance fixes are unchanged.

## Version
- versionName: **2.57.9**
- versionCode: **171**
