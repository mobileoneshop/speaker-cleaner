# DECISIONS — SonicClean

Locked on 2026-09-23 by normathi. Change only with his explicit approval.

## Locked
- **Build target:** native Android APK/AAB (NOT a web app — user chose native).
- **App name:** SonicClean — tagline "Speaker Cleaner & Water Ejector".
- **Package:** `com.normathi.soniclean` (rename allowed BEFORE M1 only).
- **Stack:** Kotlin + Jetpack Compose, single-activity, MVVM.
- **SDK:** minSdk 26, targetSdk 36, compileSdk 36.
- **Language:** English only in v1 (strings.xml ready for more later).
- **Offline-first:** no INTERNET permission, no backend, no analytics in v1.
- **Sounds synthesized at runtime** (AudioTrack) — no bundled mp3 files.
- **Builder workflow:** brain/ is fed to the AI builder; AGENT_INSTRUCTIONS.md is the
  always-on rule; microtasks run one at a time (M1→M14).

## Open / TBD
- **Monetization:** not decided. Default v1 = no ads, no in-app purchases.
  If ads are added later: add INTERNET permission deliberately + update SECURITY.md.
- **App icon final art:** spec in ICON.md; final image asset pending user approval.
- **Store order:** Amazon Appstore first vs Play Store first — undecided.

## Log
| Date | Decision |
|---|---|
| 2026-09-23 | Project created; native APK chosen over web app |
