# PRODUCTION CHECKLIST — SonicClean

Tick every box before uploading to any store.

## Build
- [ ] Release AAB built with R8 (`minifyEnabled`, `shrinkResources`, logs stripped)
- [ ] `debuggable false`, `allowBackup false`, no exported components but launcher
- [ ] Permission dump reviewed — no INTERNET in v1
- [ ] Version code bumped, version name correct (see CHANGELOG.md)
- [ ] Release build smoke-tested on physical device (full Auto Clean)

## Quality
- [ ] TESTING.md §4 gates all green
- [ ] No lorem ipsum / placeholder text anywhere (grep "lorem", "todo", "fixme")
- [ ] No commented-out code blocks
- [ ] Icon per ICON.md on launcher; splash correct; status bar themed
- [ ] Privacy policy in-app + store listing URL live

## Store assets
- [ ] Screenshots: Home, Auto Clean (mid-run), Manual, Vibration — phone + Fire HD 10
- [ ] Feature graphic 1024×500, no text smaller than readable at thumbnail size
- [ ] Short + full description from STORE_LISTING.md, spell-checked
- [ ] Content rating questionnaire done honestly (Everyone)
- [ ] Data safety form: "no data collected"

## Manual (normathi)
- [ ] Keystore backed up in two places; enable Play App Signing; turn on Play Integrity
  in Play Console before release.
- [ ] Amazon: APK (not AAB) built separately for Appstore
