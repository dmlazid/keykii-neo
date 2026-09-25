# KeyKii Neo — Google Play Publishing Checklist

## Prepared in 2.57.3
- Package: com.keykii.neo
- targetSdk: 35
- Play App Bundle build task: enabled
- Release signing supports either local keystore.properties or secure CI upload-key secrets
- Privacy-policy draft
- Store-listing copy
- Data Safety working sheet
- Ads declaration guidance
- Closed-testing checklist

## You still need to do in Google
1. Create a full-distribution Android/Play developer account. Google currently charges a one-time US$25 registration fee for full distribution.
2. Complete identity verification.
3. Create the app in Play Console with package com.keykii.neo.
4. Keep Play App Signing enabled.
5. Upload the signed KeyKii Neo 2.57.3 .aab.
6. Add store listing, icon/screenshots/feature graphic, support email and privacy-policy URL.
7. App content:
   - Privacy Policy
   - Ads: YES
   - Data Safety
   - Content rating
   - Target audience
   - App access, if requested
8. New personal accounts created after Nov. 13, 2023 currently need a closed test with at least 12 testers continuously opted in for 14 days before applying for production access.
9. After the store listing exists, link the app back to AdMob.

## Upload key
Do not commit an upload keystore or passwords to GitHub.
The workflow accepts these secure GitHub Actions secrets later:
- KEYKII_UPLOAD_KEYSTORE_B64
- KEYKII_UPLOAD_STORE_PASSWORD
- KEYKII_UPLOAD_KEY_ALIAS
- KEYKII_UPLOAD_KEY_PASSWORD

The upload key is different from the Google-held app-signing key when Play App Signing is enabled. Keep the upload keystore backed up securely.
