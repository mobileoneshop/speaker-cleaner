# DATA MODEL — SonicClean

Local-only. DataStore Preferences. Nothing leaves the device.

## Settings (`SettingsRepository`)
| Key | Type | Default |
|---|---|---|
| `theme` | String | `"system"` (`"dark"`/`"light"`/`"system"`) |
| `haptics_enabled` | Boolean | `true` |
| `safety_reminders` | Boolean | `true` |
| `default_output` | String | `"speaker"` (`"speaker"`/`"earpiece"`) |
| `disclaimer_accepted` | Boolean | `false` |
| `manual_last_freq` | Int (Hz) | `165` |

Corrupt/missing → defaults. All writes via DataStore; UI observes as StateFlow.

## Cleaning history (`HistoryRepository`)
Record per session:
```kotlin
data class CleanSession(
  val id: String,          // UUID
  val mode: String,        // "auto" | "manual" | "vibration" | "sound_test"
  val durationSec: Int,
  val peakFreqHz: Int?,    // null for vibration-only
  val output: String,      // "speaker" | "earpiece"
  val timestamp: Long      // epoch millis
)
```
- Stored as JSON list in DataStore (cap 100, oldest dropped).
- History screen reads newest-first; "Clear all" wipes with confirm dialog.

## No-Go
No user accounts, no analytics events, no device identifiers stored.
