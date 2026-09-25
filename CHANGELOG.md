# CHANGELOG — SonicClean

## 2026-09-23 — Brain created
- Full spec written: 21 files (README → API_CONTRACT).
- Locked: native Android APK, Kotlin + Compose, minSdk 26 / targetSdk 36,
  offline-first, English-only v1, runtime-synthesized audio.
- Build order: M1 → M14, one microtask at a time.

## 2026-09-23 — M1: Project scaffold
- Android project scaffold configured: Kotlin DSL with version catalog.
- Package & applicationId: `com.normathi.soniclean`.
- SDK versions: minSdk 26, targetSdk 36, compileSdk 36.
- Single `MainActivity` with edge-to-edge Compose content.
- Jetpack Navigation Compose setup with `NavHost` handling `splash` and `home` routes.
- Initial splash screen (branding & tagline) and home screen scaffold.
- Design tokens setup in `ui/theme/` (Color.kt, Type.kt, Shape.kt, Theme.kt).
- Strings centralized in `res/values/strings.xml`.
- Verified: `compile_applet` build succeeded, Robolectric unit tests passed.

## 2026-09-23 — All Screens Connected and Verified
- Implemented and wired all 8 app destinations in `SonicCleanNavGraph`:
  1. Splash (`SplashScreen`): Brand icon + tagline, 900ms auto-advance.
  2. Home (`HomeScreen`): Hero Auto Clean, Quick Clean 10s, 3 mode cards, 3-stat overview, tips carousel, header icons.
  3. Auto Clean (`AutoCleanScreen`): 34s multi-step deep cleaning cycle with circular progress ring, waveform visualizer, safety checks.
  4. Manual Mode (`ManualScreen` + `ManualViewModel`): 50-500 Hz slider, frequency presets, real-time waveform, output toggle, 60s auto-stop.
  5. Vibration Mode (`VibrationScreen` + `VibrationViewModel` + `VibrationEngine`): 3 patterns (Gentle, Deep, Pulse), 30s countdown, vibration support check.
  6. Sound Test (`SoundTestScreen` + `SoundTestViewModel`): 5s acoustic sweep (200-1000 Hz) with clarity verification ("Sounds clear?", "Run again").
  7. History (`HistoryScreen` + `HistoryViewModel`): Full cleaning log with timestamps, mode icons, duration, and clear-all confirmation.
  8. Settings (`SettingsScreen` + `SettingsViewModel` + `SettingsRepository`): Theme selection, haptics toggle, safety reminders toggle, default output routing, in-app Privacy Policy, About, Rate, and Share intents.
- Added `VIBRATE` permission in `AndroidManifest.xml`.
- Added `AllScreensUiTest` verifying rendering, UI elements, and two-way navigation for every screen.
- Verified: `compile_applet` passed, all 32 unit and Compose UI tests passed.
