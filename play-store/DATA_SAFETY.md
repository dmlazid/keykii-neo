# KeyKii Neo — Google Play Data Safety Working Sheet

This is a preparation guide, not an automatic submission. Review the final Play Console form against the actual production SDK versions before publishing.

## Data handled by KeyKii itself
- Normal keyboard typing: processed locally for text entry; not intentionally sent to KeyKii servers.
- Clipboard history: local device storage.
- Emoji recents: local device storage.
- Text shortcuts: local device storage.
- Theme/font/preferences: local device storage.
- 24-hour rewarded-access expiry: local device storage.
- Voice input: microphone permission only when the user starts voice input.
- Grammar Fix: text in the grammar box is sent to LanguageTool only after the user taps Check grammar.
- Cebuano online translation fallback: text is sent only after the user taps Translate.
- Supported ML Kit translations: designed to run on-device.

## Google Mobile Ads SDK
Google's Android disclosure says the SDK automatically collects/shares:
- IP address (may estimate general location)
- User product interactions (app launch, taps, video views)
- Diagnostic/performance information
- Device/account identifiers, including advertising ID/app set ID where available

Purposes listed by Google include advertising, analytics and fraud prevention. Google states this data is encrypted in transit.

## Suggested Play Console answers to verify
- Does the app contain ads? YES.
- Is data collected/shared by third-party SDKs? YES, because Google Mobile Ads is integrated.
- Approximate location: likely YES via IP address through Google Mobile Ads.
- App activity / user interactions: YES via Google Mobile Ads.
- App info/performance / diagnostics: YES via Google Mobile Ads.
- Device or other identifiers: YES via Google Mobile Ads.
- Audio: microphone is accessed for voice input; verify whether any voice provider/Android component transmits audio in the final implementation before completing the form.
- User-generated text: Grammar Fix and Cebuano online translation can transmit user-entered text only when the user invokes those features; declare according to Play's exact data-type categories and third-party handling.

## Security / controls
- Google Mobile Ads states its automatically collected data is encrypted in transit.
- KeyKii provides ad-consent/privacy choices through Google UMP where required.
- Local app data can be removed by clearing app data or uninstalling.
- Incognito mode pauses new clipboard-history and emoji-recent saves.

## Before production
1. Recheck Google's current Mobile Ads data-disclosure page for the SDK version in the release.
2. Verify the final LanguageTool and Cebuano fallback endpoints and their privacy policies.
3. Add the public privacy-policy URL.
4. Add your real support email.
5. Complete Play Console's exact Data Safety questionnaire yourself as the developer/publisher.
