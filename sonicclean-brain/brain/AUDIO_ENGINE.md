# AUDIO ENGINE — SonicClean

All audio parameters live in `engine/audio/AudioConfig.kt`. Never hard-code
frequencies or durations in UI code.

## Synthesis
- `SineWaveGenerator`: generates 16-bit PCM mono sine buffers, sample rate 44100 Hz.
  Pure Kotlin (no Android imports) — unit-testable: buffer length, amplitude bounds,
  zero-crossing count ≈ expected frequency.
- `AudioPlayer` (Android): streams buffers via `AudioTrack` (`MODE_STREAM`,
  `STREAM_MUSIC`). Double-buffered writes on a dedicated thread. `play(freq)`,
  `updateFrequency(freq)` (gapless), `stop()`.

## Auto Clean cycle (F1)
Total ≈ 34s. Steps from `SweepTable.AUTO_CYCLE`:
| Step | Frequency | Duration |
|---|---|---|
| 1 | 140 Hz | 8s |
| 2 | 165 Hz | 8s |
| 3 | 200 Hz | 8s |
| 4 | 250 Hz sweep → 140 Hz | 8s |
| 5 | 0.5s silence gaps between steps | — |
Vibration pulses synced at each step change (medium intensity, 300ms).
Progress = elapsed / total. Always ends by itself; UI shows summary after.

## Manual mode (F2)
- Slider range 50–500 Hz, step 1 Hz, default **165 Hz**.
- `updateFrequency` applies live with no clicks/pops (crossfade 50ms in generator).
- Auto-stop at 60s with notice "Session ended — tap play to continue."

## Vibration mode (F3)
Three patterns (via `VibrationEngine`):
- Gentle: 200ms on / 400ms off, 30s
- Deep: 500ms on / 500ms off, 30s
- Pulse: waveform [0,100,100,100,300,200], 30s
All auto-stop; respect system haptic toggle in Settings.

## Sound test (F5)
5s tone sweep 200 Hz → 1000 Hz so the user can judge clarity. Speaker only.

## Output routing (F4)
- Speaker (default): `setSpeakerphoneOn(true)`.
- Earpiece: `setSpeakerphoneOn(false)` + volume capped at 50% + bursts ≤ 10s.
- Always restore previous audio state on stop / screen exit / `onPause`.

## Safety limits (hard)
- Never play if headphones (wired or Bluetooth A2DP) are connected — block first.
- Never exceed 60s continuous playback in any mode.
- Never raise device volume programmatically without an explicit user tap.
