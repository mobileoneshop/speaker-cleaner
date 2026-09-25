# TESTING — SonicClean

## 1. Unit tests (JVM, pure Kotlin)
- `SineWaveGeneratorTest`: buffer length = sampleRate × seconds; amplitude within
  ±1.0; zero-crossings ≈ 2 × freq × seconds (±2%).
- `SweepTableTest`: AUTO_CYCLE sums to 33–35s; frequencies within 50–500 Hz.
- `AudioConfigTest`: manual range 50–500, default 165; auto-stop limits ≤ 60s.
- `HistoryRepositoryTest`: 100-record cap drops oldest; clear-all empties.
- `SettingsRepositoryTest`: corrupt DataStore → defaults; toggles persist.

## 2. UI tests (Compose)
- Home renders 4 mode cards; each navigates to its route.
- Auto Clean: Start → progress advances; Stop → resets; summary sheet appears
  on simulated finish.
- Settings: theme toggle switches palette; disclaimer dialog shows when not accepted.
- No hardcoded strings: `lintDebug` must report zero hardcoded-text warnings.

## 3. Manual QA matrix (physical device REQUIRED for audio rows)
| # | Check | Device |
|---|---|---|
| 1 | Cold start → splash → home, no jank | phone |
| 2 | Auto Clean full cycle: sound clean, no clicks, ring/waveform synced | phone |
| 3 | Headphones plugged → cleaning blocked with message | phone |
| 4 | Bluetooth headset connected → blocked | phone |
| 5 | Manual slider live pitch change, readout tracks | phone |
| 6 | Vibration patterns feel distinct | phone |
| 7 | Rotate mid-clean → state intact, no duplicate audio | phone |
| 8 | Back press during clean → audio stops, returns home | phone |
| 9 | Airplane mode → everything works (offline) | phone |
| 10 | Fire HD 10: layout, no clipped text | Fire tablet |
| 11 | Light theme: all screens readable | phone |
| 12 | 10 consecutive Auto Cleans → zero crashes | phone |

## 4. Release gates (all must be green before store upload)
- [ ] All §1–§3 pass
- [ ] R8 release build installs and runs (no shrinking crashes)
- [ ] Permission dump shows no INTERNET
- [ ] PRODUCTION_CHECKLIST.md ticked
