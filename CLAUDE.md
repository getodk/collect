# S-TIME Collect — CLAUDE.md

## Project overview

This is **S-TIME Collect**, a branded fork of [ODK Collect](https://github.com/getodk/collect) maintained by Sopami. It is a multi-module Android application for mobile data collection. The upstream project is tracked at the `upstream` remote (`git@github.com:getodk/collect.git`); the fork lives at `origin` (`git@github.com:sopami/odk-collect.git`).

## Fork-specific customizations

All branding changes relative to upstream are intentional and must be preserved when syncing with upstream:

| File | Change |
|---|---|
| `strings/src/main/res/values/untranslated.xml` | App name → `S-TIME Collect` |
| `collect_app/src/main/res/drawable/s_time_logo.xml` | S-TIME vector logo (replaces ODK logo) |
| `collect_app/src/main/res/drawable-{hdpi,mdpi,xhdpi,xxhdpi,xxxhdpi}/notes.png` | S-TIME app icon variants |
| `collect_app/src/main/res/layout/first_launch_layout.xml` | Splash uses `@drawable/s_time_logo` |
| `collect_app/src/main/res/values/theme.xml` | Splash screen animated icon → `@drawable/s_time_logo` |
| `collect_app/src/main/AndroidManifest.xml` | Provider authorities use `${applicationId}` (required after applicationId change) |
| `collect_app/google-services.json` | Sopami Firebase project config (debug) |
| `collect_app/src/release/google-services.json` | Sopami Firebase project config (release) |
| `collect_app/build.gradle` | `applicationId` → `com.sopami.collect.android`, `archivesBaseName` → `STIME-Collect` |
| `gradle.properties` | Adds `versionCode` override and `org.gradle.java.installations.auto-download=true` |

## Syncing with upstream

The upstream publishes stable releases as tags in the format `vYYYY.M.P` (e.g. `v2026.2.2`). Latest stable as of last sync: **v2026.2.2**.

To sync with a new upstream release:

```bash
git fetch upstream --tags
# Merge the target stable tag, keeping fork customizations
git merge v<VERSION> --no-ff -m "Merge upstream v<VERSION>"
# Resolve any conflicts — always keep fork-specific files listed above
```

## Build &amp; run commands

This is a standard Android Gradle multi-module project. Prerequisites:
- Android Studio Hedgehog+ or the Gradle wrapper (`./gradlew`)
- Java 17+ (Gradle will auto-download via toolchains — `org.gradle.java.installations.auto-download=true`)
- Create `secrets.properties` at the repo root with API keys if using Google Maps / Mapbox or release signing

**Build debug APK:**
```bash
./gradlew :collect_app:assembleDebug
```

**Install and run on connected device/emulator:**
```bash
./gradlew :collect_app:installDebug && \
adb shell am start -n com.sopami.collect.android/org.odk.collect.android.mainmenu.MainMenuActivity
```

**Play Store release build** (requires signing keys in `secrets.properties`):
```bash
# 1. Bump versionCode in gradle.properties (e.g. 5116)
# 2. Tag the commit for a clean versionName: git tag v1.x.x
# 3. Add to secrets.properties:
#      RELEASE_STORE_FILE=/path/to/your.keystore
#      RELEASE_STORE_PASSWORD=...
#      RELEASE_KEY_ALIAS=...
#      RELEASE_KEY_PASSWORD=...
./gradlew :collect_app:assembleRelease
# Output: collect_app/build/outputs/apk/release/collect_app-release.apk
```

**Run unit tests:**
```bash
./gradlew test
```

> **Note:** `collect_app/src/debug/google-services.json` was deleted — it contained the upstream ODK package name and blocked the debug build. All build variants now use `collect_app/google-services.json` (Sopami Firebase project). If upstream re-adds this file in a future merge, delete it again.

## Module structure

The repo is organized into many Gradle modules under the root. The main application module is `collect_app/`. Supporting library modules include (non-exhaustive): `androidshared`, `async`, `db`, `entities`, `forms`, `geo`, `maps`, `permissions`, `projects`, `settings`, `strings`, `upgrade`.

Each module follows a standard layout: `src/main/java/…`, `src/test/…`, `src/androidTest/…`.

## Application ID

`com.sopami.collect.android` (overridden from upstream's `org.odk.collect.android`). Provider authority references in `AndroidManifest.xml` use `${applicationId}` to stay in sync automatically.

## Development workflow

**Never push directly to `master`.** All changes must go through a branch and pull request.

### Starting a change

```bash
git checkout master && git pull origin master
git checkout -b <type>/<short-description>
# types: fix/ feat/ sync/ chore/
```

### Pushing and opening a PR

```bash
git push -u origin <branch-name>
gh pr create --title "..." --body "..."
```

### Merging upstream releases

Upstream syncs follow the same branch rule:

```bash
git checkout -b sync/upstream-vX.Y.Z
git fetch upstream --tags
git merge vX.Y.Z --no-ff -m "Merge upstream vX.Y.Z"
# resolve conflicts (see upstream sync instructions above)
git push -u origin sync/upstream-vX.Y.Z
gh pr create --title "Sync upstream vX.Y.Z" --body "Merges ODK Collect vX.Y.Z into the fork."
```

## Key contacts / remotes

- Upstream: `git@github.com:getodk/collect.git` (remote name: `upstream`)
- Fork: `git@github.com:sopami/odk-collect.git` (remote name: `origin`)
