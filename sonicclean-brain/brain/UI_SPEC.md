# UI SPEC — SonicClean

Premium utility aesthetic: deep dark theme by default, electric-cyan accent,
generous spacing, big tactile controls. Must NEVER look like a default template.

## Design tokens (`ui/theme/`)
- **Dark (default):** background `#0B0F1A`, surface `#141B2E`, surface-high `#1C2540`,
  text-primary `#F2F5FA`, text-secondary `#93A0B8`, accent `#00E5FF`,
  accent-gradient `#00E5FF → #7C5CFF`, success `#34D399`, warning `#FBBF24`,
  danger `#F87171`.
- **Light:** background `#F4F7FC`, surface `#FFFFFF`, surface-high `#EAF0F8`,
  text-primary `#0E1B2E`, text-secondary `#5B6B84`, accent `#0090B3`.
- **Type:** system font stack (no downloadable fonts in v1 — offline rule).
  Display 28sp/700, Title 20sp/600, Body 16sp/400, Caption 13sp/400.
- **Shape:** cards 24dp rounded, buttons pill (50%), sliders custom thumb 28dp.
- **No default Material purple anywhere.** No hardcoded colors in screens — tokens only.

## Components (`ui/components/`)
- `PrimaryButton`: pill, accent gradient, 64dp height, press scale 0.97 + haptic.
- `ModeCard`: 24dp card, icon + title + one-line subtitle, chevron; press ripple + 0.98 scale.
- `WaveformVisualizer`: Canvas bars animated from current frequency (see ANIMATIONS.md).
- `ProgressRing`: 200dp ring, gradient stroke 14dp, % + mm:ss center.
- `AppTopBar`: screen title + back arrow (every screen except Home/Splash).
- `SectionHeader`: 14sp uppercase caption style.
- `SafetyBanner`: warning-striped card for headphone/volume notices.

## Screens
1. **Splash** — centered logo mark + "SonicClean", 900ms, then Home. No buttons.
2. **Home** — greeting header, big hero `ModeCard` "Auto Clean" (gradient),
   grid of `ModeCard`s: Manual, Vibration, Sound Test; footer row: History, Settings icons.
3. **Auto Clean** — `ProgressRing` center, `WaveformVisualizer` below, step label
   ("Step 2/4 · 165 Hz"), Start/Stop `PrimaryButton`, output toggle (Speaker/Earpiece).
   On finish → summary sheet (duration, peak freq, "Test sound" + "Done").
4. **Manual** — big frequency readout (e.g. "165 Hz"), slider 50–500 Hz,
   `WaveformVisualizer`, elapsed timer, Play/Stop.
5. **Vibration** — 3 intensity cards (Gentle/Deep/Pulse), progress bar, Start/Stop.
6. **Sound Test** — "Play test tone" button, 5s sweep, "Sounds clear?" Yes/Retest.
7. **History** — list of sessions (mode, duration, date), empty state
   ("No cleanings yet — your history will appear here."), Clear all (confirm).
8. **Settings** — Theme (Dark/Light/System), Haptics toggle, Safety reminders toggle,
   Output default, About, Privacy Policy, Rate app, Share app, version footer.

## Global rules
- Status bar color matches background; edge-to-edge; no clipped text.
- Every screen reachable ≤ 3 taps from Home; every screen has back/exit.
- Empty states always have illustration + text + action. Never a blank screen.
- All strings from strings.xml (see STRINGS.md). Touch targets ≥ 56dp.
