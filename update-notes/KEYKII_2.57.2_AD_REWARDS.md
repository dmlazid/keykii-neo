# KeyKii Neo 2.57.2 — Rewarded 24-hour access + ads

## Monetization behavior
- One completed rewarded ad unlocks all PRO themes plus ad-gated fonts for **24 hours**.
- The pass is stored locally with an expiry timestamp.
- If a premium theme/font is still selected after the pass expires, KeyKii falls back to free/default styling the next time the keyboard opens.
- Theme and Font shops show the active pass and remaining time.
- PRO theme previews show **Watch ad • 24h** when the pass is inactive.
- PRO/ad-gated font previews use the same 24-hour pass.

## Random ads
- Added occasional interstitial ads only after natural completed tasks inside the KeyKii settings/shop app.
- Interstitial threshold varies between 5 and 8 completed apply actions.
- At least 4 minutes must pass between interstitials.
- No interstitial is shown at app startup, app exit, while typing, or inside the IME keyboard itself.
- Rewarded and interstitial ads are preloaded to reduce late/surprise presentation.

## Privacy
- Added Google User Messaging Platform (UMP) 4.0.0.
- Consent information is refreshed when the settings app starts.
- If Google requires a privacy-options entry point, KeyKii shows one under Privacy > Ads & rewards.

## Google Mobile Ads
- Google Mobile Ads SDK: 24.9.0 (latest supported v24 line, preserving KeyKii's minSdk 23 compatibility)
- Test fallback App ID: Google's official sample ID.
- Test rewarded/interstitial IDs are used unless production IDs are supplied.
- CI accepts repository variables:
  - `ADMOB_APP_ID`
  - `ADMOB_REWARDED_ID`
  - `ADMOB_INTERSTITIAL_ID`
- Once those GitHub repository variables contain your real AdMob IDs, new builds automatically use your monetized ad units.

## Version
- versionName: **2.57.2**
- versionCode: **164**

All 2.57.1 pricing UI, 2.57.0 theme anti-repeat behavior, and earlier keyboard performance/features remain preserved.


### Build compatibility correction
Google Mobile Ads 25.5.0 raised its minimum Android API to 24. KeyKii still supports API 23, so this build uses supported Google Mobile Ads **24.9.0** instead of dropping Android 6 compatibility.
