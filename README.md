# Tap Clash — Native Android (Jetpack Compose) Build

This repository contains a native Android implementation of *Tap Clash* (hyper-casual local multiplayer), built with Kotlin and Jetpack Compose. The game UI and core mechanics are implemented in `app/src/main/java/com/example/tapwar/MainActivity.kt`.

## What I added
- A minimal Android application module (`app/`) with Compose setup.
- `MainActivity.kt` — single-screen Compose implementation using press-time input handling (`detectTapGestures(onPress = ...)`).
- Launcher icon added from `app_icon.png` into `res/mipmap-xxxhdpi` and adaptive wrappers in `mipmap-anydpi-v26`.
- Resource strings and theme values.

## Prerequisites (local machine)
- Android Studio (Flamingo / Electric or later recommended) with Android SDK and Emulator installed.
- Java 17 (or compatible JDK configured in Android Studio).
- A physical Android device (USB + Developer Options enabled) or an emulator.

Note: I could not run a build here because `gradle`/Android SDK are not available in this environment.

## How to open and build
1. Open Android Studio -> `File > Open` -> select this repository folder.
2. Android Studio will sync Gradle and download required SDK components. Let it finish.
3. To build an APK from Android Studio: `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
4. From the command-line (if Gradle wrapper exists or after Android Studio generates it):

Windows:
```powershell
.\gradlew assembleDebug
.\gradlew installDebug   # installs to a connected device/emulator
```

macOS / Linux:
```bash
./gradlew assembleDebug
./gradlew installDebug
```

If the Gradle wrapper is not present, opening the project in Android Studio is the recommended way — it will configure and provide the wrapper for CLI usage.

## Run & Test
- Use `Run` in Android Studio to launch to an emulator or connected device.
- Test local multiplayer responsiveness by placing two fingers on the screen (one per player) and tapping rapidly.

## Notes on the implementation (performance & correctness)
- Input: Uses `detectTapGestures(onPress = { ... tryAwaitRelease() })` to register finger-down events immediately for low-latency multi-touch.
- State: Single integer `score` in `rememberSaveable { mutableIntStateOf(50) }` with derived weights (`derivedStateOf`) for layout to minimize recompositions.
- UI resizing: Zone heights use `Modifier.weight()` with `score` and `100 - score` as weights; coerced to floats to avoid zero-weight issues.
- Win condition: When `score` reaches `0` or `100`, touch inputs are disabled immediately via an `enabled` flag and a game-over overlay appears with a Reset button.

## Android compatibility & added UX features

- Added multiple mipmap density folders (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) so launchers pick an appropriate icon on different screens. Currently the same provided `app_icon.png` was copied into each for a quick compatibility pass.
- Edge-to-edge and portrait-only orientation are enabled in the manifest to match hyper-casual play assumptions.
- Haptic feedback: Light haptic feedback is triggered on every successful finger-down to improve tactile satisfaction and perceived responsiveness.
- Tap ripple: A low-cost canvas ripple effect is rendered locally inside each game zone on touch-down for visual affordance without causing heavy recomposition.
- Accessibility: Key labels use `string` resources to enable localization later; consider adding TalkBack hints and larger type scaling support.

These UX additions are lightweight and designed to keep the UI thread responsive during high-frequency tapping. For production, replace the single supplied `app_icon.png` with density-optimized assets and consider adding short audio hit effects (SoundPool) for additional feedback.

## Next recommended steps
1. Open in Android Studio and run on an emulator or device to verify end-to-end behavior.
2. Profile under heavy tapping using `Layout Inspector` and `System Traces` to ensure UI thread remains responsive.
3. Create smaller mipmap variants (mdpi/hdpi/xhdpi/xxhdpi) of the launcher foreground for optimal display.
4. Add analytics hooks and a lightweight test harness to simulate multi-touch taps in instrumentation tests.

## Sound asset (SoundPool)

The app now uses `SoundPool` to play a short tap sound when a player presses their zone. The code expects a raw resource at:

```
app/src/main/res/raw/tap_click.wav
```

Please add a short (10–80ms) uncompressed WAV or OGG file named `tap_click.wav` to that location. Recommended sources for free, permissively licensed click sounds:

- Kenney Game Assets (public domain / CC0): https://kenney.nl/assets (search "UI SFX" or "click")
- Wikimedia Commons (public domain / freely licensed) — search "click sound" at https://commons.wikimedia.org
- FreeSound (various licenses) — filter by license (CC0 recommended): https://freesound.org

When you place `tap_click.wav` in `app/src/main/res/raw/`, Android Studio will package it and the app will load it automatically at runtime.

If you want, I can attempt to download a CC0 click asset and add it to the repo for you — confirm and I will try. Note: automated download may be blocked by this environment's network policy; I will report back if so.

## Files of interest
- `app/src/main/java/com/example/tapwar/MainActivity.kt` — game logic & UI
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` — supplied icon (copied from `app_icon.png`)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` — adaptive icon wrapper

If you want, I can:
- Add Gradle wrapper files here (requires a Gradle runtime to generate securely), or
- Produce automated tests (Espresso/UiAutomator) that simulate taps, or
- Build an equivalent React Native/Expo version for cross-platform parity.
