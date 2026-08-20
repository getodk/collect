---
name: Hotfix release
title: '🔥 vXXXX.X.X'
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

- a `secrets.properties` file in the root project folder with the following:
  ```
  // secrets.properties
  RELEASE_STORE_FILE=/path/to/collect.keystore
  RELEASE_STORE_PASSWORD=secure-store-password
  RELEASE_KEY_ALIAS=key-alias
  RELEASE_KEY_PASSWORD=secure-alias-password
  ```

- a `google-services.json` file in the `collect_app/src/odkCollectRelease` folder. The contents of the file are similar to the contents of `collect_app/src/google-services.json`.

## Checklist

- [ ] make sure CI is green for the chosen commit
- [ ] run `./gradlew releaseCheck`
- [ ] verify a basic "happy path": scan a QR code to configure a new project, get a blank form, fill it, open the form map (confirms that the Google Maps key is correct), send form
- [ ] verify new APK can be installed as update to previous version and that above "happy path" works in that case also
- [ ] Tag the commit for the patch release (`vX.X.X`)
    - If beta has started for next release:
        - [ ] Tag the commit for the beta release (`vX.X.X-beta.X`)
- [ ] Run `./create-release.sh <last release version code> <patch release tag> <beta release tag>`
- [ ] add a release(s) to GitHub [here](https://github.com/getodk/collect/releases), generate release notes and attach the APK
- [ ] upload APK(s) to Play Store
    - The beta APK should be uploaded second as it will have a higher version code
- [ ] backup dependencies for the release by downloading the `vX.X.X.tar` artifact from the `create_dependency_backup` job on Circle CI (for the release commit) and then uploading it to [this folder](https://drive.google.com/drive/folders/1_tMKBFLdhzFZF9GKNeob4FbARjdfbtJu?usp=share_link)
- [ ] delete previous dependency backups for this major version (i.e. if this hotfix is v3000.1.2, backups for v3000.1.1 and v3000.1.0 should be deleted)
- [ ] backup a self-signed release APK by downloading the `selfSignedRelease.apk` from the `build_release` job on Circle CI (for the release commit) and then upload to [this folder](https://drive.google.com/drive/folders/1pbbeNaMTziFhtZmedOs0If3BeYu3Ex5x?usp=share_link)
- [ ] merge hotfix branch changes back into master