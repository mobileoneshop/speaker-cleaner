# ANIMATIONS — SonicClean

Motion is what separates "premium" from "vibe-coded." Follow these exactly.

## Global motion language
- Screen transitions: fade + 24dp vertical slide, **300ms**, `FastOutSlowInEasing`.
- Button press: scale to 0.97 over 100ms, release spring back (stiffness 400).
- Cards: press scale 0.98 + ripple; no bounce on cards (only buttons).
- All animations must respect "reduce motion" is out of scope v1 — keep them subtle.

## WaveformVisualizer (signature animation)
- Canvas, 48 bars, rounded 4dp caps, accent gradient fill.
- Bar heights driven by the *actual playing frequency*: height = base ×
  (0.6 + 0.4 × sin(t × freq/50 + i × 0.35)), smoothed with lerp 0.2/frame.
- 60fps target; pause rendering when screen not visible or audio stopped
  (bars settle to flat baseline over 400ms — never freeze mid-air).
- Manual mode: bars react live to slider changes.

## ProgressRing (Auto Clean)
- 200dp, 14dp gradient stroke, track `#1C2540` (dark) / `#EAF0F8` (light).
- Sweep animates with progress; number counts 0→100 in sync; step changes pulse
  the ring (scale 1.0→1.04→1.0, 250ms).
- On finish: ring completes, 400ms glow pulse, then summary sheet slides up.

## Splash
- Logo mark scale 0.8→1.0 + fade in, 500ms; tagline fades in 200ms later.
- Total 900ms → navigate Home with fade. No user interaction.

## Micro-interactions
- Frequency slider: thumb grows 28→34dp while dragging; readout counts smoothly
  (animateIntAsState, 150ms) — number must track the thumb, never jump.
- Mode select: selected card border glows accent, 200ms.
- Start cleaning: button morphs to Stop (color → danger, label crossfades 200ms).
- Summary sheet: slide up 300ms + scrim fade; dismiss on scrim tap or Done.
- Toggles: standard Material3 switch with accent thumb.
- Haptic: light tick on button press, medium on start/stop (if enabled in Settings).

## Forbidden
- No infinite spinners without progress context; no jarring pops (always ease);
- no animation longer than 600ms except the cleaning progress itself;
- never leave a view half-animated on navigation — cancel in `DisposableEffect`.
