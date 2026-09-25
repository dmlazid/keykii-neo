# AdMob setup before Play publication

KeyKii does NOT need to already be published on Google Play in order to create AdMob IDs.

Google AdMob supports adding an unpublished Android app:
1. Sign in to AdMob.
2. Apps > Add app.
3. Choose Android.
4. Choose that the app is NOT listed on a supported app store.
5. Name it KeyKii Neo.
6. Create:
   - one Rewarded ad unit for the 24-hour unlock
   - one Interstitial ad unit for natural-break ads
7. Copy the AdMob App ID, Rewarded Ad Unit ID and Interstitial Ad Unit ID.

Until real IDs are configured, KeyKii intentionally uses Google's official test IDs so development/test taps do not create invalid traffic.
