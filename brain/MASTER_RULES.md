# MASTER RULES — SonicClean

These rules override everything else. If a task conflicts with a rule, follow the rule
and flag the conflict.

1. **Stack is fixed:** Kotlin + Jetpack Compose, single-activity, MVVM. No XML layouts
   for new screens, no Flutter/React Native, no new frameworks without a DECISIONS.md entry.
2. **SDK range:** minSdk 26, targetSdk 36, compileSdk 36. Never use APIs above 26 without
   a runtime version guard (`Build.VERSION.SDK_INT` check) or AndroidX backport.
3. **Offline-first, always:** the app must work fully offline. Do NOT add the INTERNET
   permission. No analytics SDKs, no crash-reporting SDKs in v1. (Ads are TBD — see
   DECISIONS.md; if ads are added later, INTERNET gets added then, deliberately.)
4. **Privacy:** collect zero personal data. Nothing leaves the device. Cleaning history
   stays in local DataStore. See SECURITY.md.
5. **No hard-coded user-visible strings.** Every string goes in `res/values/strings.xml`
   (English, ready for future localization). See STRINGS.md.
6. **No magic numbers for audio config.** Frequencies, durations, sweep steps live in
   `AudioConfig` — never inline in UI code. See AUDIO_ENGINE.md.
7. **Audio math is pure Kotlin.** Waveform synthesis (`SineWaveGenerator`, sweep tables)
   must not import Android classes so it is unit-testable on JVM. Android audio
   (AudioTrack) lives behind an interface.
8. **Premium-UX is law (see UI_SPEC.md + ANIMATIONS.md):** custom dark/light themes
   (never default Material colors), touch targets ≥ 56dp, every screen has visible
   back/exit, loading states everywhere (never a blank screen), haptic feedback on
   primary actions, 300ms screen transitions.
9. **Safety first (see PRD.md § Safety):** headphone check before any cleaning mode,
   max-volume guidance, auto-stop timers, first-run hearing disclaimer. Never bypass.
10. **Assets by convention:** icon per ICON.md; sounds are synthesized at runtime
    (no bundled audio files in v1).
11. **Audio failure = graceful fallback,** never a crash. AudioTrack init failure →
    show error state per ERROR_HANDLING.md.
12. **Dependencies are minimal.** Before adding any library: check TRD.md's approved list.
    New dependency needs a DECISIONS.md entry with why.
13. **Every microtask ends with:** code compiles, related unit tests pass, and the
    acceptance criteria in MICROTASKS.md are met. Update CHANGELOG.md per task.
14. **Commit style:** `M<n>: <short description>` matching MICROTASKS.md ids.
15. **Real-device verification:** audio/vibration features MUST be verified on a physical
    device (emulator speakers don't represent reality). State the device used in reports.
