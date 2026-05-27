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

## Build setup

This is a standard Android Gradle multi-module project. Prerequisites:
- Android Studio Hedgehog+ or the Gradle wrapper (`./gradlew`)
- Java 17+ (Gradle will auto-download via toolchains — `org.gradle.java.installations.auto-download=true`)
- Create `secrets.gradle` (or `secrets.properties`) with API keys if using Google Maps / Mapbox

Build the debug APK:
```bash
./gradlew :collect_app:assembleDebug
```

Run unit tests:
```bash
./gradlew test
```

## Module structure

The repo is organized into many Gradle modules under the root. The main application module is `collect_app/`. Supporting library modules include (non-exhaustive): `androidshared`, `async`, `db`, `entities`, `forms`, `geo`, `maps`, `permissions`, `projects`, `settings`, `strings`, `upgrade`.

Each module follows a standard layout: `src/main/java/…`, `src/test/…`, `src/androidTest/…`.

## Application ID

`com.sopami.collect.android` (overridden from upstream's `org.odk.collect.android`). Provider authority references in `AndroidManifest.xml` use `${applicationId}` to stay in sync automatically.

## Key contacts / remotes

- Upstream: `git@github.com:getodk/collect.git` (remote name: `upstream`)
- Fork: `git@github.com:sopami/odk-collect.git` (remote name: `origin`)
