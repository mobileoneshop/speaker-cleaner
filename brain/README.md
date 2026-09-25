# BRAIN — SonicClean (Speaker Cleaner & Water Ejector)

Native Android app spec. This folder is the source of truth for the AI builder.

**App:** SonicClean — Speaker Cleaner & Water Ejector
**Package:** `com.normathi.soniclean` (changeable before M1 — see DECISIONS.md)
**Stack:** Kotlin + Jetpack Compose, single-activity, MVVM
**Goal:** a premium, best-in-class speaker cleaner app — great animations,
zero "vibe-coded" feel. Ships as APK/AAB (Amazon Appstore + Play Store).

## Read order for the builder
1. `AGENT_INSTRUCTIONS.md` — paste into the builder's custom instructions
2. `MASTER_RULES.md` — locked rules, override everything
3. `PRD.md` — what the app does
4. `TRD.md` + `AUDIO_ENGINE.md` — how it's built
5. `UI_SPEC.md` + `ANIMATIONS.md` — how it looks and moves
6. `MICROTASKS.md` — build order M1→M14, one at a time

## File map
| File | What it is |
|---|---|
| AGENT_INSTRUCTIONS.md | Operating rules for the AI builder |
| MASTER_RULES.md | Locked rules (stack, SDK, privacy, quality) |
| PRD.md | Product requirements, features, safety |
| TRD.md | Technical requirements, architecture |
| AUDIO_ENGINE.md | Sound synthesis spec (frequencies, safety) |
| UI_SPEC.md | Design system + all screens |
| ANIMATIONS.md | Every animation, timings, motion rules |
| DATA_MODEL.md | Settings + history (DataStore) |
| STRINGS.md | All user-visible English strings |
| MICROTASKS.md | M1–M14 build order with acceptance criteria |
| TESTING.md | Unit / UI / manual QA plan |
| ERROR_HANDLING.md | Failure behavior, fallbacks |
| SECURITY.md | R8, tamper protection, privacy |
| PRODUCTION_CHECKLIST.md | Release gate |
| STORE_LISTING.md | Store copy, screenshots, privacy policy |
| ICON.md | App icon spec |
| DECISIONS.md | Locked decisions + open TBDs |
| CHANGELOG.md | Per-microtask progress log |
| ADMOB.md | Monetization — TBD (see DECISIONS.md) |
| API_CONTRACT.md | Empty — no backend, fully offline |

## Non-goals (v1)
No backend, no accounts, no analytics SDKs, no INTERNET permission.
