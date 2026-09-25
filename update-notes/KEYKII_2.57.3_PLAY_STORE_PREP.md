# KeyKii Neo 2.57.3 — Play Store preparation

This update prepares KeyKii Neo for Google Play without changing the working keyboard behavior.

## Build / signing
- versionName: 2.57.3
- versionCode: 165
- GitHub Actions now builds both the debug APK and a release Android App Bundle (.aab).
- Release signing supports a local keystore.properties file or secure GitHub Actions upload-key secrets.
- If no upload key is configured in CI, the AAB artifact is intentionally unsigned and can be signed offline with the private upload key.

## Play Store package
Added:
- privacy policy draft
- Play Store listing draft
- Data Safety working sheet
- publishing checklist
- AdMob unpublished-app setup guide

## Existing behavior preserved
- 2.57.2 rewarded-ad 24-hour access
- interstitial natural-break rules
- UMP consent/privacy choices
- 2.57.1 pricing UI
- 2.57.0 anti-repeat theme ordering
- all keyboard tools and 2.54.4 performance fixes
