# MICROTASKS — SonicClean

Work in order. Each task: implement → compile → tests pass → acceptance criteria met →
CHANGELOG.md entry → commit `M<n>: <desc>`.

## M1 — Project scaffold
Android project (Kotlin DSL, version catalog): applicationId `com.normathi.soniclean`,
minSdk 26, targetSdk/compileSdk 36, Compose BOM, single `MainActivity`, NavHost with
`splash` and `home` routes.
_Accept: app installs and launches to splash on an API 26 device._

## M2 — Design system
`ui/theme/`: Color.kt (dark + light palettes from UI_SPEC.md), Type.kt, Shape.kt.
Components: `PrimaryButton`, `ModeCard`, `SectionHeader`, `AppTopBar` with previews.
_Accept: preview screen renders all components in both themes; zero hardcoded colors
in screens; no default Material purple anywhere._

## M3 — Audio engine core (pure Kotlin)
`SineWaveGenerator`, `SweepTable` (AUTO_CYCLE table), `AudioConfig` per AUDIO_ENGINE.md.
Zero `android.*` imports.
_Accept: JVM unit tests — buffer length/amplitude/frequency accuracy, sweep table
totals ≈ 34s, manual range 50–500 Hz._

## M4 — AudioPlayer (Android)
`AudioPlayer` wrapping AudioTrack (44100 Hz, 16-bit PCM mono, MODE_STREAM):
`play(freq)`, `updateFrequency(freq)` gapless, `stop()`. `AudioOutputRouter`
(speaker/earpiece). Init-failure → error state.
_Accept: on a physical device — 165 Hz tone plays clean with no clicks; earpiece
mode routes correctly and restores state on stop._

## M5 — Vibration engine + safety checks
`VibrationEngine` (VibratorManager w/ API 26–30 fallback), 3 patterns from
AUDIO_ENGINE.md. `SafetyChecks`: headphone detection, first-run disclaimer state.
_Accept: on device — each pattern feels distinct; headphones connected → blocked._

## M6 — Settings + history storage
`SettingsRepository`, `HistoryRepository` (DataStore) per DATA_MODEL.md.
_Accept: unit tests — defaults on corrupt data, 100-record cap, clear-all._

## M7 — Splash + Home
Splash animation per ANIMATIONS.md (900ms → Home). Home screen per UI_SPEC.md with
hero Auto Clean card + mode grid + History/Settings.
_Accept: cold start → splash → home < 1.5s; all cards navigate to correct routes._

## M8 — Auto Clean screen
`ProgressRing`, `WaveformVisualizer`, step label, Start/Stop, output toggle,
summary sheet, history write on finish. Full AUDIO_ENGINE.md cycle.
_Accept: on device — full 34s cycle runs, ring/waveform in sync, summary shows,
session saved to history, audio stops on navigate-away._

## M9 — Manual mode screen
Frequency readout + slider (50–500 Hz), live waveform, elapsed timer, Play/Stop,
60s auto-stop.
_Accept: on device — slider changes pitch live with no pops; readout tracks thumb._

## M10 — Vibration + Sound Test screens
Vibration screen: 3 intensity cards, progress, Start/Stop. Sound Test: 5s sweep,
"clear?" prompt.
_Accept: on device — patterns distinct; test tone plays; both auto-stop._

## M11 — History + Settings screens
History list, empty state, clear-all confirm. Settings per UI_SPEC.md §8, incl.
theme switching, disclaimer re-view, About, Privacy Policy (in-app text),
Rate/Share intents.
_Accept: theme switches instantly; toggles persist across restart._

## M12 — Polish pass (anti-vibe-code)
Screen transitions 300ms everywhere; haptics on primary actions; status bar +
edge-to-edge; rotation retains state (ViewModels); all strings in strings.xml
(`lintDebug` zero hardcoded-string warnings); no dead buttons; SafetyBanner
states (headphones/volume) verified.
_Accept: lint clean; rotate mid-clean on phone — state intact, audio managed._

## M13 — Full test sweep
Run TESTING.md §1–§3 completely (unit, UI, manual QA matrix incl. Fire tablet).
_Accept: all gates in TESTING.md §4 green._

## M14 — Release build + store prep
R8 (`minifyEnabled`, `shrinkResources`, log stripping) per SECURITY.md; signed
release AAB; permission dump (no INTERNET); store assets per STORE_LISTING.md;
privacy policy finalized.
_Accept: PRODUCTION_CHECKLIST.md fully ticked.
