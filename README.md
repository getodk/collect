# ODK Collect
![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/opendatakit/collect.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/opendatakit/collect)
[![codecov.io](https://codecov.io/github/opendatakit/collect/branch/master/graph/badge.svg)](https://codecov.io/github/opendatakit/collect)
[![Slack status](http://slack.opendatakit.org/badge.svg)](http://slack.opendatakit.org)

ODK Collect is an Android app for filling out forms. It is designed to be used in resource-constrained environments with challenges such as unreliable connectivity or power infrastructure. ODK Collect is part of Open Data Kit (ODK), a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the Open Data Kit project and its history [here](https://opendatakit.org/about/) and read about example ODK deployments [here](https://opendatakit.org/about/deployments/).

ODK Collect renders forms that are compliant with the [ODK XForms standard](http://opendatakit.github.io/xforms-spec/), a subset of the [XForms 1.1 standard](https://www.w3.org/TR/xforms/) with some extensions. The form parsing is done by the [JavaRosa library](https://github.com/opendatakit/javarosa) which Collect includes as a dependency.

## Table of Contents
* [Learn more about ODK Collect](#learn-more-about-odk-collect)
* [Release cycle](#release-cycle)
* [Setting up your development environment](#setting-up-your-development-environment)
* [Testing a form without a server](#testing-a-form-without-a-server)
* [Using APIs for local development](#using-apis-for-local-development)
* [Debugging JavaRosa](#debugging-javarosa)
* [Contributing code](#contributing-code)
* [Contributing translations](#contributing-translations)
* [Contributing testing](#contributing-testing)
* [Downloading builds](#downloading-builds)
* [Creating signed releases for Google Play Store](#creating-signed-releases-for-google-play-store)
* [Troubleshooting](#troubleshooting)

## Learn more about ODK Collect
* ODK website: [https://opendatakit.org](https://opendatakit.org)
* ODK Collect usage documentation: [https://docs.opendatakit.org/collect-intro/](https://docs.opendatakit.org/collect-intro/)
* ODK forum: [https://forum.opendatakit.org](https://forum.opendatakit.org)
* ODK developer Slack chat: [http://slack.opendatakit.org](http://slack.opendatakit.org) 
* ODK developer Slack archive: [https://opendatakit.slackarchive.io](https://opendatakit.slackarchive.io) 

## Release cycle
New versions of ODK Collect are generally released on the last Sunday of a month. We freeze commits to the master branch on the preceding Wednesday (except for bug fixes). Releases can be requested by any community member and generally happen every 2 months. [@yanokwa](https://github.com/yanokwa) pushes the releases to the Play Store.

At the beginning of each release cycle, [@grzesiek2010](https://github.com/grzesiek2010) updates all dependencies that have compatible upgrades available and ensures that the build targets the latest SDK.

## Suggesting new features
We try to make sure that all issues in the issue tracker are as close to fully specified as possible so that they can be closed by a pull request. Feature suggestions should be described [in the forum Features category](https://forum.opendatakit.org/c/features) and discussed by the broader user community. Once there is a clear way forward, issues should be filed on the relevant repositories. More controversial features will be discussed as part of the Technical Steering Committee's [roadmapping process](https://github.com/opendatakit/governance/tree/master/TSC1#roadmapping).

## Setting up your development environment

1. Download and install [Git](https://git-scm.com/downloads) and add it to your PATH

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) 

1. Fork the collect project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/collect

    If you prefer not to use the command line, you can use Android Studio to create a new project from version control using `https://github.com/YOUR-GITHUB-USERNAME/collect`.

1. Open the project in the folder of your clone from Android Studio. To run the project, click on the green arrow at the top of the screen.

1. Make sure you can run unit tests by running everything under `collect_app/src/test/java` in Android Studio or on the command line:

    ```
    ./gradlew testDebug
    ```

1. Make sure you can run instrumented tests by running everything under `collect_app/src/androidTest/java` in Android Studio or on the command line:

    ```
    ./gradlew connectedAndroidTest
    ```
    **Note:** You can see the emulator setup used on CI in  `.circleci/config.yml`.

## Testing a form without a server
When you first run Collect, it is set to download forms from [https://opendatakit.appspot.com/](https://opendatakit.appspot.com/), the demo server. You can sometimes verify your changes with those forms but it can also be helpful to put a specific test form on your device. Here are some options for that:

1. The `All Widgets` form from the default Aggregate server is [here](https://docs.google.com/spreadsheets/d/1af_Sl8A_L8_EULbhRLHVl8OclCfco09Hq2tqb9CslwQ/edit#gid=0). You can also try [example forms](https://github.com/XLSForm/example-forms) and [test forms](https://github.com/XLSForm/test-forms) or [make your own](https://xlsform.org).

1. Convert the XLSForm (xlsx) to XForm (xml). Use the [ODK website](http://opendatakit.org/xiframe/) or [XLSForm Offline](https://gumroad.com/l/xlsform-offline) or [pyxform](https://github.com/XLSForm/pyxform).

1. Once you have the XForm, use [adb](https://developer.android.com/studio/command-line/adb.html) to push the form to your device (after [enabling USB debugging](https://www.kingoapp.com/root-tutorials/how-to-enable-usb-debugging-mode-on-android.htm)) or emulator.
	```
	adb push my_form.xml /sdcard/odk/forms/
	```

1. Launch ODK Collect and tap `Fill Blank Form`. The new form will be there.

## Using APIs for local development

Certain functions in ODK Collect depend on cloud services that require API keys or authorization steps to work.  Here are the steps you need to take in order to use these functions in your development builds.

**Google Drive and Sheets APIs**: When the "Google Drive, Google Sheets" option is selected in the "Server" settings, ODK Collect uses these APIs to store submitted form data in Google Sheets and submitted media in Google Drive.  To enable these APIs:
  - Follow [these instructions to generate a signing certificate fingerprint and register the application with the Google API Console](https://developers.google.com/drive/android/auth#generate_the_signing_certificate_fingerprint_and_register_your_application).
  - [Enable the Google Drive API](https://console.developers.google.com/apis/api/drive.googleapis.com).
  - [Enable the Google Sheets API](https://console.developers.google.com/apis/api/sheets.googleapis.com).

**Google Maps API**: When the "Google Maps SDK" option is selected in the "User interface" settings, ODK Collect uses the Google Maps API for displaying maps in the geospatial widgets (GeoPoint, GeoTrace, and GeoShape).  To enable this API:
  - [Get a Google Maps API key](https://developers.google.com/maps/documentation/android-api/signup).  Note that this requires a credit card number, though the card will not be charged immediately; some free API usage is permitted.  You should carefully read the terms before providing a credit card number.
  - Edit or create `collect_app/secrets.properties` and set the `GOOGLE_MAPS_API_KEY` property to your API key.  You should end up with a line that looks like this:
    ```
    GOOGLE_MAPS_API_KEY=AIbzvW8e0ub...
    ```

**Mapbox Maps SDK for Android**: When the "Mapbox SDK" option is selected in the "User interface" settings, ODK Collect uses the Mapbox SDK for displaying maps in the geospatial widgets (GeoPoint, GeoTrace, and GeoShape).  To enable this API:
  - [Create a Mapbox account](https://www.mapbox.com/signup/).  Note that signing up with the "Pay-As-You-Go" plan does not require a credit card.  Mapbox provides free API usage up to the monthly thresholds documented at [https://www.mapbox.com/pricing](https://www.mapbox.com/pricing).  If your usage exceeds these thresholds, you will receive e-mail with instructions on how to add a credit card for payment; services will remain live until the end of the 30-day billing term, after which the account will be deactivated and will require a credit card to reactivate.
  - Find your access token on your [account page](https://account.mapbox.com/).
  - Edit or create `collect_app/secrets.properties` and set the `MAPBOX_ACCESS_TOKEN` property to your access token.  You should end up with a line that looks like this:
    ```
    MAPBOX_ACCESS_TOKEN=pk.eyJk3bumVp4i...
    ```

## Debugging JavaRosa

JavaRosa is the form engine that powers Collect. If you want to debug or change that code while running Collect, you have two options. You can include the source tree as a module in Android Studio or include a custom jar file you build.

**Source tree**

1. Get the code from the [JavaRosa repo](https://github.com/opendatakit/javarosa)
1. In Android Studio, select `File` -> `New` -> `New Module` -> `Import Gradle Project` and choose the project
1. In Collect's `build.gradle` file, find the JavaRosa section:
    ```gradle
    implementation("org.opendatakit:opendatakit-javarosa:x.y.z") {
        ...
    }
    ```
1. Replace the first line like this, using `javarosa` or whatever name you specified when importing:
    ```gradle
    implementation (project(path: ':javarosa')) {
        ...
    }
    ```

**Jar file**

1. In JavaRosa, change the version in `build.gradle` and build the jar
	```gradle
	jar {
	    baseName = 'opendatakit-javarosa'
	    version = 'x.y.z-SNAPSHOT'
	```

1. In Collect, add the path to the jar to the dependencies in `build.gradle`
	```gradle
	compile files('/path/to/javarosa/build/libs/opendatakit-javarosa-x.y.z-SNAPSHOT.jar')
	```

## Contributing code
Any and all contributions to the project are welcome. ODK Collect is used across the world primarily by organizations with a social purpose so you can have real impact!

Issues tagged as [good first issue](https://github.com/opendatakit/collect/labels/good%20first%20issue) should be a good place to start. There are also currently many issues tagged as [needs reproduction](https://github.com/opendatakit/collect/labels/needs%20reproduction) which need someone to try to reproduce them with the current version of ODK Collect and comment on the issue with their findings.

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

## Contributing translations
If you know a language other than English, consider contributing translations through [Transifex](https://www.transifex.com/opendatakit/collect/). 

Translations are updated right before the first beta for a release and before the release itself. To update translations, download the zip from https://www.transifex.com/opendatakit/collect/strings/. The contents of each folder then need to be moved to the Android project folders. A quick script like [the one in this gist](https://gist.github.com/lognaturel/9974fab4e7579fac034511cd4944176b) can help. We currently copy everything from Transifex to minimize manual intervention. Sometimes translation files will only get comment changes. When new languages are updated in Transifex, they need to be added to the script above. Additionally, `ApplicationConstants.TRANSLATIONS_AVAILABLE` needs to be updated. This array provides the choices for the language preference in general settings. Ideally the list could be dynamically generated.

## Contributing testing
All pull requests are verified on the following devices (ordered by Android version):
* [Infinix Race Bolt Q X451](http://bestmobs.com/infinix-race-bolt-q-x451) - Android 4.2.1
* [Samsung Galaxy J1 SM-J100H](http://www.gsmarena.com/samsung_galaxy_j1-6907.php) - Android 4.4.4
* [Huawei Y560-L01](http://www.gsmarena.com/huawei_y560-7829.php) - Android 5.1.1
* [Sony Xperia Z3 D6603](http://www.gsmarena.com/sony_xperia_z3-6539.php) - Android 6.0.1
* [Samsung Galaxy S7 SM-G930F](https://www.gsmarena.com/samsung_galaxy_s7-7821.php) - Android 7.0.0
* [LG Nexus 5X](https://www.gsmarena.com/lg_nexus_5x-7556.php) - Android 8.1

Our regular code contributors use these devices (ordered by Android version): 
* [Alcatel One Touch 5020D](https://www.gsmarena.com/alcatel_one_touch_m_pop-5242.php) - Android 4.1.1
* [Xiaomi Redmi Note 4](https://www.gsmarena.com/xiaomi_redmi_note_4-8531.php) - Android 7.0
* [Samsung Galaxy S4 GT-I9506](http://www.gsmarena.com/samsung_i9506_galaxy_s4-5542.php) - Android 5.0.1
* [Samsung Galaxy Tab SM-T285](http://www.gsmarena.com/samsung_galaxy_tab_a_7_0_(2016)-7880.php) - Android 5.1.1
* [Motorola G 5th Gen XT1671](https://www.gsmarena.com/motorola_moto_g5-8454.php) - Android 7.0

The best way to help us test is to build from source! If you aren't a developer and want to help us test release candidates, join the [beta program](https://play.google.com/apps/testing/org.odk.collect.android)!

Testing checklists can be found on the [Collect testing plan](https://docs.google.com/spreadsheets/d/1ITmOW2MFs_8-VM6MTwganTRWDjpctz9CI8QKojXrnjE/edit?usp=sharing).

If you have finished testing a pull request, please use a template from [Testing result templates](.github/TESTING_RESULT_TEMPLATES.md) to report your insights.

## Downloading builds
Per-commit debug builds can be found on [CircleCI](https://circleci.com/gh/opendatakit/collect). Login with your GitHub account, click the build you'd like, then find the APK in the Artifacts tab.

Current and previous production builds can be found on the [ODK website](https://opendatakit.org/downloads/download-info/odk-collect-apk).

## Creating signed releases for Google Play Store
Project maintainers have the keys to upload signed releases to the Play Store. 

Maintainers have a `secrets.properties` file in the `collect_app` folder with the following:
```
// collect_app/secrets.properties
RELEASE_STORE_FILE=/path/to/collect.keystore
RELEASE_STORE_PASSWORD=secure-store-password
RELEASE_KEY_ALIAS=key-alias
RELEASE_KEY_PASSWORD=secure-alias-password
```

Maintainers also have a `google-services.json` file in the `collect_app/src/odkCollectRelease` folder. The contents of the file are similar to the contents of `collect_app/src/google-services.json`.

To generate official signed releases, you'll need the keystore file, the keystore passwords, a configured `collect_app/secrets.properties` file, and a configured `collect_app/src/odkCollectRelease/google-services.json` file. Then run `./gradlew assembleOdkCollectRelease`. If successful, a signed release will be at `collect_app/build/outputs/apk`.

## Troubleshooting

#### Error when running Robolectric tests from Android Studio on macOS: `build/intermediates/bundles/debug/AndroidManifest.xml (No such file or directory)`
> Configure the default JUnit test runner configuration in order to work around a bug where IntelliJ / Android Studio does not set the working directory to the module being tested. This can be accomplished by editing the run configurations, Defaults -> JUnit and changing the working directory value to $MODULE_DIR$.

> Source: [Robolectric Wiki](https://github.com/robolectric/robolectric/wiki/Running-tests-in-Android-Studio#notes-for-mac).

#### Android Studio Error: `SDK location not found. Define location with sdk.dir in the local.properties file or with an ANDROID_HOME environment variable.`
When cloning the project from Android Studio, click "No" when prompted to open the `build.gradle` file and then open project.

#### Execution failed for task ':collect_app:transformClassesWithInstantRunForDebug'.

We have seen this problem happen in both IntelliJ IDEA and Android Studio, and believe it to be due to a bug in the IDE, which we can't fix.  As a workaround, turning off [Instant Run](https://developer.android.com/studio/run/#set-up-ir) will usually avoid this problem.  We haven't yet found a way to use Instant Run with this project but this will most likely be fixed in Android Studio 3.5 with the new [Apply Changes](https://medium.com/androiddevelopers/android-studio-project-marble-apply-changes-e3048662e8cd) feature.

#### Moving to the main view if user minimizes the app
If you build the app on your own using Android Studio `(Build -> Build APK)` and then install it (from an `.apk` file), you might notice this strange behaviour thoroughly described: [#1280](https://github.com/opendatakit/collect/issues/1280) and [#1142](https://github.com/opendatakit/collect/issues/1142).

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

`export JAVA_HOME="/Applications/Android Studio.app/Contents/jre/jdk/Contents/Home"
`

Note that this change might cause problems with other Java-based applications (e.g., if you uninstall Android Studio).

#### gradlew Failure: `java.lang.NullPointerException (no error message).`
If you encounter the `java.lang.NullPointerException (no error message).` when running `gradlew`, please make sure your Java version for this project is Java 8.

This can be configured under **File > Project Structure** in Android Studio, or by editing `$USER_HOME/.gradle/gradle.properties` to set `org.gradle.java.home=(path to JDK home)` for command-line use.
