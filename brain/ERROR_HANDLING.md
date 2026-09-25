# ERROR HANDLING — SonicClean

Never crash. Every failure becomes a clear on-screen state.

| Failure | Behavior |
|---|---|
| AudioTrack init fails | Error card on screen: "Couldn't access the speaker. Restart the app and try again." + Retry button. Log locally. |
| Headphones connected at start | Block with SafetyBanner: "Disconnect headphones to protect your hearing." Start button disabled until resolved. |
| Headphones plugged mid-clean | Stop immediately, show notice "Stopped — headphones detected." |
| Phone call / audio focus lost | Pause cleaning, show "Paused — call in progress", resume only on user tap. |
| Vibration unavailable | Vibration mode card shows "Not supported on this device", disabled. |
| DataStore corrupt | Fall back to defaults, continue silently. |
| History write fails | Continue session; skip history entry; one debug log. |
| Rotation / process death | ViewModel retains state; audio restarts only on explicit user tap — never auto-resume sound. |
| Earpiece mode on device without earpiece | Fall back to speaker with notice. |

All user-visible messages come from strings.xml. No stack traces on screen, ever.
