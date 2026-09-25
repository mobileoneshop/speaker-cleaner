# TRD — SonicClean (Technical Requirements)

## Stack
- Kotlin, Jetpack Compose (BOM), single `MainActivity`, Compose Navigation.
- Kotlin DSL Gradle + version catalog (`gradle/libs.versions.toml`).
- MVVM: `ui/` (screens) → `viewmodel/` → `engine/` + `data/`.

## Approved dependencies (v1)
- androidx core-ktx, lifecycle-viewmodel-compose, navigation-compose
- androidx.datastore-preferences (settings + history)
- Compose BOM (material3, ui-tooling-preview)
Nothing else without a DECISIONS.md entry.

## Module layout
```
app/src/main/java/com/normathi/soniclean/
  MainActivity.kt
  nav/NavGraph.kt
  ui/theme/      Color.kt, Type.kt, Shape.kt
  ui/components/ Buttons.kt, ModeCard.kt, WaveformVisualizer.kt, ProgressRing.kt,
                 SectionHeader.kt, AppTopBar.kt
  ui/screens/    SplashScreen.kt, HomeScreen.kt, AutoCleanScreen.kt,
                 ManualScreen.kt, VibrationScreen.kt, SoundTestScreen.kt,
                 HistoryScreen.kt, SettingsScreen.kt
  viewmodel/     AutoCleanViewModel.kt, ManualViewModel.kt, VibrationViewModel.kt,
                 SettingsViewModel.kt, HistoryViewModel.kt
  engine/audio/  AudioConfig.kt, SineWaveGenerator.kt (pure Kotlin),
                 SweepTable.kt (pure Kotlin), AudioPlayer.kt (AudioTrack impl),
                 AudioOutputRouter.kt
  engine/haptics/ VibrationEngine.kt (VibratorManager wrapper)
  engine/safety/ SafetyChecks.kt (headphone detection, disclaimers)
  data/          SettingsRepository.kt, HistoryRepository.kt (DataStore)
```

## Key technical notes
- `SineWaveGenerator` / `SweepTable`: pure Kotlin, zero `android.*` imports → JVM unit tests.
- `AudioPlayer`: wraps `AudioTrack` (STREAM_MUSIC, 44100 Hz, 16-bit PCM mono,
  `MODE_STREAM`). Exposes `play(frequencyHz)`, `stop()`, `isPlaying`. Handles
  init failure → error state (see ERROR_HANDLING.md).
- Headphone detection: `AudioManager.getDevices(GET_DEVICES_OUTPUTS)` — block if any
  wired/wireless (non-speaker, non-earpiece) device is connected.
- Earpiece routing: `AudioManager.setSpeakerphoneOn(false)` + `MODE_IN_CALL`-style
  routing for F4; restore on stop/exit.
- Volume: read `STREAM_MUSIC` volume; prompt user to raise to max via system UI —
  never set it programmatically without explicit user tap.
- Lifecycle: stop audio + vibration in `onPause`/`onStop` and when navigating away.
  Never play sound from a backgrounded app.
- Haptics: `VibratorManager` (API 31+) with `Vibrator` fallback (API 26–30) behind
  version guards.
- Themes: full dark + light palettes in Color.kt; follow system by default.
