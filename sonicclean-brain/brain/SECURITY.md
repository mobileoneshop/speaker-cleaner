# SECURITY — SonicClean

## Build hardening (M14)
- Release: `minifyEnabled true`, `shrinkResources true`,
  `proguard-android-optimize.txt`; strip `android.util.Log` via
  `-assumenosideeffects` (keep `Log.e` for crash diagnostics → route to nothing
  in release; no remote reporting in v1).
- `debuggable false`; no exported components except launcher activity.
- `android:allowBackup="false"`.

## Secrets & privacy
- No API keys, tokens, or endpoints exist in v1 (fully offline). If any are added
  later: local.properties / BuildConfig, never committed.
- Zero personal data collected. History stays on-device. Privacy policy must state
  this plainly (see STORE_LISTING.md).

## Tamper protection (v1-lite)
- Play Integrity API check at startup on Play builds; on failed verdict show
  "This app may have been modified" and disable cleaning (Amazon builds: skip,
  log locally).
- Basic root detection fallback (su binary, test-keys) — warn only, don't brick.

## Network
- `cleartextTrafficPermitted=false`; no network calls in v1, so nothing to pin.
  If ads/networking are added later, add certificate pinning then.

## Manual TODO for normathi (not the builder)
- Keystore backup in two places; enable Play App Signing; turn on Play Integrity
  in Play Console before release.
