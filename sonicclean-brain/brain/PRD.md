# PRD — SonicClean (Speaker Cleaner & Water Ejector)

## One-liner
A premium Android utility that plays precisely engineered sound frequencies and
vibration patterns to shake dust and water out of phone speakers.

## Target user
Anyone whose phone speaker sounds muffled after dust exposure or water contact.
Non-technical; expects "tap one button, wait, done."

## v1 Features

### F1 — Auto Clean (hero feature)
One-tap preset cycle: app plays an engineered frequency sweep (~32s) with synced
vibration pulses and an animated progress ring + live waveform visualizer.
Ends with a summary (duration, peak frequency) and a "Test sound" prompt.

### F2 — Manual Mode
Frequency slider (50–500 Hz, default 165 Hz), Play/Stop, live waveform visualizer,
elapsed timer. For users who want to find their device's "sweet spot."

### F3 — Vibration Mode
Haptic-only cleaning patterns (3 intensities). Works with sound off. Uses
`VibratorManager`; falls back gracefully on devices without rich haptics.

### F4 — Output selector
Speaker / Earpiece toggle. Earpiece mode uses lower volume + shorter bursts.

### F5 — Sound test
After cleaning, play a short test tone sweep so the user can judge clarity.

### F6 — Cleaning history
Local log of sessions (mode, duration, date). View in History screen; clear-all option.

### F7 — Settings
Theme (Dark/Light/System), haptic feedback toggle, safety reminders toggle,
About, Privacy Policy (in-app), Rate app, Share app.

## Safety (non-negotiable)
- **Headphone check:** before F1/F2/F3, detect wired/Bluetooth audio output. If
  headphones connected → block with message "Disconnect headphones to protect
  your hearing."
- **First-run disclaimer:** one-time dialog — loud sounds, keep volume guidance,
  "I understand" to continue.
- **Auto-stop:** Auto Clean always ends by itself (≤ 40s). Manual mode auto-stops
  at 60s with a notice.
- **Volume guidance:** prompt user to set media volume to max for best results,
  but never change volume silently — ask first via system dialog.

## Out of scope (v1)
Accounts, cloud sync, ads (TBD), widgets, Wear OS, iOS.

## Success criteria
- Installs and runs offline on minSdk 26 device with zero crashes in a 10-session QA run.
- A non-technical user completes Auto Clean without instructions beyond on-screen text.
- Looks and feels premium: custom theme, smooth animations, no placeholder content.
