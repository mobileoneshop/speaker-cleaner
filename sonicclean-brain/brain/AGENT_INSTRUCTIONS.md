# AGENT INSTRUCTIONS — SonicClean

Paste this entire file into your AI builder's custom/agent instructions. It keeps
the AI strictly on-task: it does ONLY what the user asks, nothing extra.

---

You are building **SonicClean** (package `com.normathi.soniclean`), a native Android
speaker-cleaner app in Kotlin + Jetpack Compose. The `brain/` folder holds 21 spec
files — they are the source of truth.

## Operating rules (never break these)

1. **Read before you act.** Before ANY work, read `brain/README.md`,
   `brain/MASTER_RULES.md`, and the spec files relevant to the task.
2. **Only the named microtask.** Work ONLY on the microtask the user names
   (e.g. "do M4" from `brain/MICROTASKS.md`). Never start another microtask on your own.
3. **No scope creep.** NEVER modify files outside the current microtask's scope.
   No drive-by refactors, no "improvements" to unrelated code, no renaming things
   you weren't asked to touch.
4. **No new dependencies.** NEVER add a library without asking the user first AND
   logging it in `brain/DECISIONS.md`.
5. **Locked decisions are locked.** App name, package, Kotlin + Compose, minSdk 26 /
   targetSdk 36, offline-first (no INTERNET permission), English-only v1, audio engine
   parameters in `brain/AUDIO_ENGINE.md`, design system in `brain/UI_SPEC.md` — do NOT
   change any of these without the user's explicit approval.
6. **Spec conflicts.** If the user's request conflicts with a spec file, STOP and flag
   the conflict. Do not silently override the spec.
7. **Finish clean, then stop.** After a microtask: compile, run its tests, verify the
   acceptance criteria in `brain/MICROTASKS.md`, append a `brain/CHANGELOG.md` entry,
   then STOP and report. Wait for the user before starting the next microtask.
8. **Ask before deleting** any file or code.
9. **Short reports.** Say what you changed, which tests passed, and how the user can
   verify it on a real device. No long essays.
10. **Change requests:** modify ONLY what was asked; summarize the diff.
11. **Test requests:** run the relevant tests from `brain/TESTING.md` and report
    pass/fail honestly. Never claim tests passed without running them.
12. **Anti-vibe-code law.** Every screen must follow `brain/UI_SPEC.md` and
    `brain/ANIMATIONS.md` exactly. No default Material purple, no placeholder text,
    no dead buttons, no blank states without loading UI. If it looks AI-generated,
    it is wrong — redo it.

## How the project flows
M1 → M2 → … → M14, one microtask at a time, each approved by the user before the
next begins. `implementation_plan.md` (if present) is your own scratch file for the
current microtask's plan — show it, get approval, then execute.
