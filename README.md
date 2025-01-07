# ODK Collect

![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/getodk/collect.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/getodk/collect)
[![Slack](https://img.shields.io/badge/chat-on%20slack-brightgreen)](https://slack.getodk.org)

ODK Collect is an Android app for filling out forms. It is designed to be used in resource-constrained environments with challenges such as unreliable connectivity or power infrastructure. ODK Collect is part the ODK project, a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about ODK and its history [here](https://getodk.org/) and read about example ODK deployments [here](https://forum.getodk.org/c/showcase).

ODK Collect renders forms that are compliant with the [ODK XForms standard](https://getodk.github.io/xforms-spec/), a subset of the [XForms 1.1 standard](https://www.w3.org/TR/xforms/) with some extensions. The form parsing is done by the [JavaRosa library](https://github.com/getodk/javarosa) which Collect includes as a dependency.

Please note that the `master` branch reflects ongoing development and is not production-ready.

## Table of Contents
* [Learn more about ODK Collect](#learn-more-about-odk-collect)
* [Release cycle](#release-cycle)
* [Downloading builds](#downloading-builds)
* [Suggesting new features](#suggesting-new-features)
* Contributing
  * [Contributing code](#contributing-code)
  * [Contributing translations](#contributing-translations)
* Developing
  * [Setting up your development environment](#setting-up-your-development-environment)
  * [Testing a form without a server](#testing-a-form-without-a-server)
  * [Using APIs for local development](#using-apis-for-local-development)
  * [Debugging JavaRosa](#debugging-javarosa)
  * [Troubleshooting](#troubleshooting)
  * [Test devices](#test-devices)
* [Creating signed releases for Google Play Store](#creating-signed-releases-for-google-play-store)

## Learn more about ODK Collect
* ODK website: [https://getodk.org](https://getodk.org)
* ODK Collect usage documentation: [https://docs.getodk.org/collect-intro/](https://docs.getodk.org/collect-intro/)
* ODK forum: [https://forum.getodk.org](https://forum.getodk.org)
* ODK developer Slack chat: [https://slack.getodk.org](https://slack.getodk.org)

## Release cycle

Releases are planned to happen every 2-3 months (resulting in ~4 releases a year). Soon before (or just after) the end of one release cycle, the core team will plan a new set of work for the next release. This involves:

1. Moving issues not finished in the last release and new items from the [ODK Roadmap](https://getodk.org/roadmap) to the [planning board](https://github.com/orgs/getodk/projects/9/views/25)
2. Giving the core team a few days to review and reflect on the planning board
3. The core team will then meet to trim work that will not be included in the next release and pitch alternative things to work on
4. The milestone for the new release is added to [the backlog](https://github.com/orgs/getodk/projects/9) and is prioritized

Sometimes issues will be assigned to core team members before they are actually started (moved to "in progress") to make it clear who's going to be working on what.

Once the majority of high risk or visible work is done for a release, a new beta will then be released to the Play Store by [@lognaturel](https://github.com/lognaturel) and that will be used for regression testing by [@getodk/testers](https://github.com/orgs/getodk/teams/testers). If any problems are found, the release is blocked until we can merge fixes. Regression testing should continue on the original beta build (rather than a new one with fixes) unless problems block the rest of testing. Once the process is complete, [@lognaturel](https://github.com/lognaturel) pushes the releases to the Play Store following [these instructions](#creating-signed-releases-for-google-play-store).

Fixes to a previous release should be merged to a "release" branch (`v2023.2.x` for example) so as to leave `master` available for the current release's work. If hotfix changes are needed in the current release as well then these can be merged in as a PR after hotfix releases (generally easiest as a single PR for the whole hotfix release). This approach can also be used if work for the next release starts before the current one is out - the next release continues on `master` while the release is on a release branch.

At the beginning of each release cycle, [@grzesiek2010](https://github.com/grzesiek2010) updates all dependencies that have compatible upgrades available and ensures that the build targets the latest SDK.

## Downloading builds
Per-commit debug builds can be found on [CircleCI](https://circleci.com/gh/getodk/collect). Login with your GitHub account, click the build you'd like, then find the APK in the Artifacts tab.

If you are looking to use ODK Collect, we strongly recommend using the [Play Store build](https://play.google.com/store/apps/details?id=org.odk.collect.android). Current and previous production builds can be found in [Releases](https://github.com/getodk/collect/releases).

## Suggesting new features
We try to make sure that all issues in the issue tracker are as close to fully specified as possible so that they can be closed by a pull request. Feature suggestions should be described [in the forum Features category](https://forum.getodk.org/c/features) and discussed by the broader user community. Once there is a clear way forward, issues should be filed on the relevant repositories. More controversial features will be discussed as part of the Technical Steering Committee's [roadmapping process](https://github.com/getodk/governance/blob/master/TSC-1/STANDARD-OPERATING-PROCEDURES.md#roadmap).

## Contributing code
Any and all contributions to the project are welcome. ODK Collect is used across the world primarily by organizations with a social purpose so you can have real impact!

Issues tagged as [good first issue](https://github.com/getodk/collect/labels/good%20first%20issue) should be a good place to start. There are also currently many issues tagged as [needs reproduction](https://github.com/getodk/collect/labels/needs%20reproduction) which need someone to try to reproduce them with the current version of ODK Collect and comment on the issue with their findings.

If you're ready to contribute code, see [the contribution guide](docs/CONTRIBUTING.md).

## Contributing translations
If you know a language other than English, consider contributing translations through [Transifex](https://www.transifex.com/getodk/collect/).

Translations are updated right before the first beta for a release and before the release itself. To update translations, download the zip from https://www.transifex.com/getodk/collect/strings/. The contents of each folder then need to be moved to the Android project folders. A quick script like [the one in this gist](https://gist.github.com/lognaturel/9974fab4e7579fac034511cd4944176b) can help. We currently copy everything from Transifex to minimize manual intervention. Sometimes translation files will only get comment changes. When new languages are updated in Transifex, they need to be added to the script above. Additionally, `ApplicationConstants.TRANSLATIONS_AVAILABLE` needs to be updated. This array provides the choices for the language preference in settings. Ideally the list could be dynamically generated.

## Setting up your development environment

1. Download and install [Git](https://git-scm.com/downloads) and add it to your PATH

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) 

1. Fork the collect project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/collect

    If you prefer not to use the command line, you can use Android Studio to create a new project from version control using `https://github.com/YOUR-GITHUB-USERNAME/collect`.

1. Use Android Studio to import the project from its Gradle settings. To run the project, click on the green arrow at the top of the screen.

1. Windows developers: continue configuring Android Studio with the steps in this document: [Developing ODK Collect on Windows](docs/WINDOWS-DEV-SETUP.md).

1. Make sure you can run unit tests by running everything under `collect_app/src/test/java` in Android Studio or on the command line:

    ```
    ./gradlew testDebug
    ```

1. Make sure you can run instrumented tests by running everything under `collect_app/src/androidTest/java` in Android Studio or on the command line:

    ```
    ./gradlew connectedAndroidTest
    ```
    **Note:** You can see the emulator setup used on CI in  `.circleci/config.yml`.

## Customizing the development environment

### Changing JVM heap size

You can customize the heap size that is used for compiling and running tests. Increasing these will most likely speed up compilation and tests on your local machine. The default values are specified in the project's `gradle.properties` and this can be overriden by your user level `gradle.properties` (found in your `GRADLE_USER_HOME` directory). An example `gradle.properties` that would give you a heap size of 4GB (rather than the default 1GB) would look like:

```
org.gradle.jvmargs=-Xmx4096m
```

## Testing a form without a server
When you first run Collect, it is set to download forms from [https://demo.getodk.org/](https://demo.getodk.org/), the demo server. You can sometimes verify your changes with those forms but it can also be helpful to put a specific test form on your device. Here are some options for that:

1. The `All question types` form from the default server is [here](https://docs.google.com/spreadsheets/d/1af_Sl8A_L8_EULbhRLHVl8OclCfco09Hq2tqb9CslwQ/edit#gid=0). You can also try [example forms](https://github.com/XLSForm/example-forms) and [test forms](https://github.com/XLSForm/test-forms) or [make your own](https://xlsform.org).

1. Convert the XLSForm (xlsx) to XForm (xml). Use the [ODK website](http://getodk.org/xlsform/) or [XLSForm Offline](https://gumroad.com/l/xlsform-offline) or [pyxform](https://github.com/XLSForm/pyxform).

1. Once you have the XForm, use [adb](https://developer.android.com/studio/command-line/adb.html) to push the form to your device (after [enabling USB debugging](https://www.kingoapp.com/root-tutorials/how-to-enable-usb-debugging-mode-on-android.htm)) or emulator.
	```
	adb push my_form.xml /sdcard/Android/data/org.odk.collect.android/files/projects/{project-id}/forms
	```

If you are using the demo project, kindly replace `{project_id}` with `DEMO`

4. Launch ODK Collect and tap `+ Start new form`. The new form will be there.

More information about using Android Debug Bridge with Collect can be found [here](https://docs.getodk.org/collect-adb/).

## Using APIs for local development

Certain functions in ODK Collect depend on cloud services that require API keys or authorization steps to work.  Here are the steps you need to take in order to use these functions in your development builds.

**Google Maps API**: When the "Google Maps SDK" option is selected in the "User interface" settings, ODK Collect uses the Google Maps API for displaying maps in the geospatial question types (GeoPoint, GeoTrace, and GeoShape).  To enable this API:
  1. [Get a Google Maps API key](https://developers.google.com/maps/documentation/android-api/signup).  Note that this requires a credit card number, though the card will not be charged immediately; some free API usage is permitted.  You should carefully read the terms before providing a credit card number.
  1. Edit or create `secrets.properties` and set the `GOOGLE_MAPS_API_KEY` property to your API key.  You should end up with a line that looks like this:
    ```
    GOOGLE_MAPS_API_KEY=AIbzvW8e0ub...
    ```

**Mapbox Maps SDK for Android**: When the "Mapbox SDK" option is selected in the "User interface" settings, ODK Collect uses the Mapbox SDK for displaying maps in the geospatial question types (GeoPoint, GeoTrace, and GeoShape).  To enable this API:
  1. [Create a Mapbox account](https://www.mapbox.com/signup/).  Note that signing up with the "Pay-As-You-Go" plan does not require a credit card.  Mapbox provides free API usage up to the monthly thresholds documented at [https://www.mapbox.com/pricing](https://www.mapbox.com/pricing).  If your usage exceeds these thresholds, you will receive e-mail with instructions on how to add a credit card for payment; services will remain live until the end of the 30-day billing term, after which the account will be deactivated and will require a credit card to reactivate.
  2. Find your access token on your [account page](https://account.mapbox.com/) - it should be in "Tokens" as "Default public token".
  3. Edit or create `secrets.properties` and set the `MAPBOX_ACCESS_TOKEN` property to your access token.  You should end up with a line that looks like this:
    ```
    MAPBOX_ACCESS_TOKEN=pk.eyJk3bumVp4i...
    ```
  4. Create a new secret token with the "DOWNLOADS:READ" secret scope and then add it to `secrets.properties` as `MAPBOX_DOWNLOADS_TOKEN`.

*Note: Mapbox will not be available as an option in compiled versions of Collect unless you follow the steps above. Mapbox will also not be available on x86 devices as the native libraries are excluded to reduce the APK size. If you need to use an x86 device, you can force the build to include x86 libs by include the `x86Libs` Gradle parameter. For example, to build a debug APK with x86 libs: `./gradlew assembleDebug -Px86Libs`.*

## Debugging JavaRosa

JavaRosa is the form engine that powers Collect. If you want to debug or change that code while running Collect you can deploy it locally with Maven (you'll need `mvn` and `sed` installed):

1. Build and install your changes of JavaRosa (into your local Maven repo):

```bash
./gradlew publishToMavenLocal
```

1. Change `const val javarosa = javarosa_online` in `Dependencies.kt` to `const val javarosa = javarosa_local`

## Troubleshooting

#### Error when running Robolectric tests from Android Studio on macOS: `build/intermediates/bundles/debug/AndroidManifest.xml (No such file or directory)`
> Configure the default JUnit test runner configuration in order to work around a bug where IntelliJ / Android Studio does not set the working directory to the module being tested. This can be accomplished by editing the run configurations, Defaults -> JUnit and changing the working directory value to $MODULE_DIR$.

> Source: [Robolectric Wiki](https://github.com/robolectric/robolectric/wiki/Running-tests-in-Android-Studio#notes-for-mac).

#### Android Studio Error: `SDK location not found. Define location with sdk.dir in the local.properties file or with an ANDROID_HOME environment variable.`
When cloning the project from Android Studio, click "No" when prompted to open the `build.gradle` file and then open project.

#### Execution failed for task ':collect_app:transformClassesWithInstantRunForDebug'.

We have seen this problem happen in both IntelliJ IDEA and Android Studio, and believe it to be due to a bug in the IDE, which we can't fix.  As a workaround, turning off [Instant Run](https://developer.android.com/studio/run/#set-up-ir) will usually avoid this problem. The problem is fixed in Android Studio 3.5 with the new [Apply Changes](https://medium.com/androiddevelopers/android-studio-project-marble-apply-changes-e3048662e8cd) feature.

#### Moving to the main view if user minimizes the app
If you build the app on your own using Android Studio `(Build -> Build APK)` and then install it (from an `.apk` file), you might notice this strange behaviour thoroughly described: [#1280](https://github.com/getodk/collect/issues/1280) and [#1142](https://github.com/getodk/collect/issues/1142).

This problem occurs building other apps as well.

#### gradlew Failure: `FAILURE: Build failed with an exception.`

If you encounter an error similar to this when running `gradlew`:

```
FAILURE: Build failed with an exception

What went wrong:
A problem occurred configuring project ':collect_app'.
> Failed to notify project evaluation listener.
   > Could not initialize class com.android.sdklib.repository.AndroidSdkHandler
```

You may have a mismatch between the embedded Android SDK Java and the JDK installed on your machine. You may wish to set your **JAVA_HOME** environment variable to that SDK. For example, on macOS:

`export JAVA_HOME="/Applications/Android\ Studio.app/Contents/jre/Contents/Home/"
`

Note that this change might cause problems with other Java-based applications (e.g., if you uninstall Android Studio).

#### gradlew Failure: `java.lang.NullPointerException (no error message).`
If you encounter the `java.lang.NullPointerException (no error message).` when running `gradlew`, please make sure your Java version for this project is Java 17.

This can be configured under **File > Project Structure** in Android Studio, or by editing `$USER_HOME/.gradle/gradle.properties` to set `org.gradle.java.home=(path to JDK home)` for command-line use.

#### `Unable to resolve artifact: Missing` while running tests

This is encountered when Robolectric has problems downloading the jars it needs for different Android SDK levels. If you keep running into this you can download the JARs locally and point Robolectric to them by doing:

```
./download-robolectric-deps.sh
```

## Test devices

Devices that @getodk/testers have available for testing are as follows:

* Xiaomi Redmi 9T 4GB - Android 10
* Pixel 7a 8GB - Android 14
* LG Nexus 5X 2GB - Android 8.1
* Samsung Galaxy M12 4GB - Android 11
* Samsung Galaxy M23 4GB - Android 14
* Xiaomi Redmi 7 3GB - Android 10
* Pixel 6a 6GB - Android 13
* Pixel 3a 4GB - Android 12
* Huawei Y560-L01 1GB - Android 5.1

## Creating signed releases for Google Play Store
Maintainers keep a folder with a clean checkout of the code and use [jenv.be](https://www.jenv.be) in that folder to ensure compilation with Java 17.

### Release prerequisites:

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

### Release checklist:

- update translations
- make sure CI is green for the chosen commit
- run `./gradlew releaseCheck`. If successful, a signed release will be at `collect_app/build/outputs/apk` (with an old version name)
- verify a basic "happy path": scan a QR code to configure a new project, get a blank form, fill it, open the form map (confirms that the Google Maps key is correct), send form
- run `./benchmark.sh` with a real device connected to verify performance
- verify new APK can be installed as update to previous version and that above "happy path" works in that case also
- create and publish scheduled forum post with release description
- write Play Store release notes, include link to forum post
- create a release with the correct version by tagging the commit and running `./collect_app:assembleOdkCollectRelease`
  - Tags for full releases must have the format `vX.X.X`. Tags for beta releases must have the format `vX.X.X-beta.X`.
- add a release to Github [here](https://github.com/getodk/collect/releases), generate release notes and attach the APK
- upload APK to Play Store
- if there was an active beta before release (this can happen with point releases), publish a new beta release to replace the previous one which was disabled by the production release
- backup dependencies for the release by downloading the `vX.X.X.tar` artifact from the `create_dependency_backup` job on Circle CI (for the release commit) and then uploading it to [this folder](https://drive.google.com/drive/folders/1_tMKBFLdhzFZF9GKNeob4FbARjdfbtJu?usp=share_link)
- backup a self signed release APK by downloading the `selfSignedRelease.apk` from the `build_release` job on Circle CI (for the release commit) and then upload to [this folder](https://drive.google.com/drive/folders/1pbbeNaMTziFhtZmedOs0If3BeYu3Ex5x?usp=share_link)

## Compiling a previous release using backed-up dependencies

1. Download the `.tar` for relevant release tag
2. Extract `.local-m2` into the project directory:
    ```bash
    tar -xf maven.tar -C <collect project directory>
    ```
   
The project will now be able to fetch dependencies that are no longer available (but were used to compile the release) from the `.local-m2` Maven repo.

