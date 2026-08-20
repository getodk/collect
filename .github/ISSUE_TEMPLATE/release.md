---
name: Release
title: '🚢 vXXXX.X.0'
labels: ''
assignees: ''
---

## Prerequisites

Maintainers keep a folder with a clean checkout of the code and use [jenv.be](https://www.jenv.be) in that folder to ensure compilation with Java 17.

- a`local.properties` file in the root folder with the following:
  ```
  sdk.dir=/path/to/android/sdk
  ```

- the keystore file and passwords

- a `secrets.properties` file in the root project folder folder with the following:
  ```
  // secrets.properties
  RELEASE_STORE_FILE=/path/to/collect.keystore
  RELEASE_STORE_PASSWORD=secure-store-password
  RELEASE_KEY_ALIAS=key-alias
  RELEASE_KEY_PASSWORD=secure-alias-password
  ```

- a `google-services.json` file in the `collect_app/src/odkCollectRelease` folder. The contents of the file are similar to the contents of `collect_app/src/google-services.json`.

## Checklist

- [ ] update translations
- [ ]make sure CI is green for the chosen commit
- [ ] run `./gradlew releaseCheck`. If successful, a signed release will be at `collect_app/build/outputs/apk` (with an old version name)
- [ ] verify a basic "happy path": scan a QR code to configure a new project, get a blank form, fill it, open the form map (confirms that the Google Maps key is correct), send form
- [ ] run `./benchmark.sh` with a real device connected to verify performance
    - To run benchmarks a project will need to be set up in Central with the benchmark forms and app users. The forms and entities needed for that are available [here](https://drive.google.com/drive/folders/1dPLvDY0LhVX-5qTUEs6EDoraDnLpUS0g?usp=drive_link).
- [ ] verify new APK can be installed as update to previous version and that above "happy path" works in that case also
- [ ] create and publish scheduled forum post with release description
- [ ] write Play Store release notes, include link to forum post
- when creating a major release:
    - [ ] Tag the commit for the release (`vX.X.0`)
    - [ ] Run `./create-release.sh <last release version code> <release tag>`
- when creating a patch release:
    - [ ] Tag the commit for the patch release (`vX.X.X`)
    - If beta has started for next release:
      - [ ] Tag the commit for the beta release (`vX.X.X-beta.X`)
    - [ ] Run `./create-release.sh <last release version code> <patch release tag> <beta release tag>`
- when creating a beta release:
    - [ ] Tag the commit for the beta release (`vX.X.X-beta.X`)
    - [ ] Run `./create-release.sh <last release version code> <beta release tag>`
- [ ] add a release to Github [here](https://github.com/getodk/collect/releases), generate release notes and attach the APK
- [ ] upload APK(s) to Play Store
    - When creating a hotfix, the beta APK should be uploaded second as it will have a higher version code
- [ ] backup dependencies for the release by downloading the `vX.X.X.tar` artifact from the `create_dependency_backup` job on Circle CI (for the release commit) and then uploading it to [this folder](https://drive.google.com/drive/folders/1_tMKBFLdhzFZF9GKNeob4FbARjdfbtJu?usp=share_link)
- [ ] backup a self signed release APK by downloading the `selfSignedRelease.apk` from the `build_release` job on Circle CI (for the release commit) and then upload to [this folder](https://drive.google.com/drive/folders/1pbbeNaMTziFhtZmedOs0If3BeYu3Ex5x?usp=share_link)