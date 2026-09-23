# KeyKii Neo 2.10.2

The app icon, the keyboard's settings shortcut and Android's keyboard settings
entry open the same settings hub. Enable KeyKii and Choose Keyboard are available
at the top. Version labels come from the installed app.

Emoji and kaomoji use continuous vertical lists. Category buttons jump within
those lists. The spacebar cursor gesture implementation is retained from 2.10.1.

## Build in your existing Codespace

From the project folder, after pulling the latest changes:

```bash
./gradlew :app:assembleDebug
```

Download `app/build/outputs/apk/debug/app-debug.apk`.
Use the same Codespace and signing key as the installed app to install an update
over it. Existing `keystore.properties` release signing is still supported with
`./gradlew :app:assembleRelease`.

## GitHub Actions

The Build KeyKii APK workflow compiles a debug APK when source changes reach
main, and can also be started manually from Actions. Download the
`KeyKii-Neo-debug` artifact from a successful run.

The GitHub runner uses its own debug signing key. That APK is for testing; it
may not install over an APK signed in your Codespace. Use your original build
environment and key for an update that keeps existing app data. Do not upload
private signing keys or passwords into this public repository.
