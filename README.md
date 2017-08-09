# ODK Collect
![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/opendatakit/collect.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/opendatakit/collect)
[![Slack status](http://slack.opendatakit.org/badge.svg)](http://slack.opendatakit.org)

ODK Collect is an Android app for filling out forms. It is designed to be used in resource-constrained environments with challenges such as unreliable connectivity or power infrastructure. ODK Collect is part of Open Data Kit (ODK), a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the Open Data Kit project and its history [here](https://opendatakit.org/about/) and read about example ODK deployments [here](https://opendatakit.org/about/deployments/).

ODK Collect renders forms that are compliant with the [ODK XForms standard](http://opendatakit.github.io/xforms-spec/), a subset of the [XForms 1.1 standard](https://www.w3.org/TR/xforms/) with some extensions. The form parsing is done by the [JavaRosa library](https://github.com/opendatakit/javarosa) which Collect includes as a jar.

* ODK website: [https://opendatakit.org](https://opendatakit.org)
* ODK Collect usage instructions: [https://opendatakit.org/use/collect](https://opendatakit.org/use/collect/)
* ODK forum: [https://forum.opendatakit.org](https://forum.opendatakit.org)
* ODK developer Slack chat: [http://slack.opendatakit.org](http://slack.opendatakit.org) 
* ODK developer Slack archive: [http://opendatakit.slackarchive.io](http://opendatakit.slackarchive.io) 
* ODK developer wiki: [https://github.com/opendatakit/opendatakit/wiki](https://github.com/opendatakit/opendatakit/wiki)

## Release cycle
New versions of ODK Collect are released on the last Sunday of each month. We freeze commits to the master branch on the preceding Wednesday (except for bug fixes).

## Testing a form locally

1. The `All Widgets` form from the default Aggregate server is [here](https://docs.google.com/spreadsheets/d/1af_Sl8A_L8_EULbhRLHVl8OclCfco09Hq2tqb9CslwQ/edit#gid=0). You can also try [example forms](https://github.com/XLSForm/example-forms) and [test forms](https://github.com/XLSForm/test-forms) or [make your own](https://xlsform.org).

1. Convert the XLSForm (xlsx) to XForm (xml). Use the [ODK website](http://opendatakit.org/xiframe/) or [XLSForm Offline](https://gumroad.com/l/xlsform-offline) or [pyxform](https://github.com/XLSForm/pyxform).

1. Once you have the XForm, use [adb](https://developer.android.com/studio/command-line/adb.html) to push the form to your device (after [enabling USB debugging](https://www.kingoapp.com/root-tutorials/how-to-enable-usb-debugging-mode-on-android.htm)) or emulator.
	```
	adb push my_form.xml /sdcard/odk/forms/
	```

1. Launch ODK Collect and tap `Fill Blank Form`. The new form will be there.

## Setting up your development environment

1. Download and install [Git](https://git-scm.com/downloads) and add it to your PATH

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) 

1. Fork the collect project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/collect

 If you prefer not to use the command line, you can use Android Studio to create a new project from version control using `https://github.com/YOUR-GITHUB-USERNAME/collect`. 

1. Open the project in the folder of your clone from Android Studio. To run the project, click on the green arrow at the top of the screen. The emulator is very slow so we generally recommend using a physical device when possible.

## Using APIs for local development

To run functionality that makes API calls from your debug-signed builds, you may need to get an API key or otherwise authorize your app.

**Google Drive and Sheets APIs** - Follow the instructions in the "Generate the signing certificate fingerprint and register your application" section from [here](https://developers.google.com/drive/android/auth). Enable the Google Drive API [here](https://console.developers.google.com/apis/api/drive/). Enable the Google Sheets API [here](https://console.developers.google.com/apis/api/sheets.googleapis.com).

**Google Maps API** - Follow the instructions [here](https://developers.google.com/maps/documentation/android-api/signup). Please be sure not to commit your personal API key to a branch that you will submit a pull request for.
 
## Contributing code
Any and all contributions to the project are welcome. ODK Collect is used across the world primarily by organizations with a social purpose so you can have real impact!

Issues tagged as [quick win](https://github.com/opendatakit/collect/labels/quick%20win) should be a good place to start. There are also currently many issues tagged as [needs reproduction](https://github.com/opendatakit/collect/labels/needs%20reproduction) which need someone to try to reproduce them with the current version of ODK Collect and comment on the issue with their findings.

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

## Contributing translations
If you know a language other than English, consider contributing translations through [Transifex](https://www.transifex.com/opendatakit/collect/).

## Contributing testing
All releases are verified on the following devices (ordered by Android version):
* [Samsung Galaxy Young GT-S6310](http://www.gsmarena.com/samsung_galaxy_young_s6310-5280.php) - Android 4.1.2
* [Infinix Race Bolt Q X451](http://bestmobs.com/infinix-race-bolt-q-x451) - Android 4.2.1
* [Samsung Galaxy J1 SM-J100H](http://www.gsmarena.com/samsung_galaxy_j1-6907.php) - Android 4.4.4
* [Huawei Y560-L01](http://www.gsmarena.com/huawei_y560-7829.php) - Android 5.1.1
* [Sony Xperia Z3 D6603](http://www.gsmarena.com/sony_xperia_z3-6539.php) -Android 6.0.1

Our regular code contributors use these devices (ordered by Android version): 
* [XOLO Q700s plus](http://www.gsmarena.com/xolo_q700s_plus-6624.php) - Android 4.4.2
* [Samsung Galaxy S4 GT-I9506](http://www.gsmarena.com/samsung_i9506_galaxy_s4-5542.php) - Android 5.0.1
* [Samsung Galaxy Tab SM-T285](http://www.gsmarena.com/samsung_galaxy_tab_a_7_0_(2016)-7880.php) - Android 5.1.1
* [Motorola G4 4th Gen XT1625](http://www.gsmarena.com/motorola_moto_g4-8103.php) - Android 7.0

The best way to help us test is to build from source! If you aren't a developer and want to help us test release candidates, join the [beta program](https://play.google.com/apps/testing/org.odk.collect.android)!

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
To generate official signed releases, you'll need the keystore file, the keystore passwords, a configured `secrets.properties` file, and then run `./gradlew assembleRelease`. If successful, a signed release will be at `collect_app/build/outputs/apk`.

## Troubleshooting
#### Error when running Robolectric tests from Android Studio on macOS: `build/intermediates/bundles/debug/AndroidManifest.xml (No such file or directory)`
> Configure the default JUnit test runner configuration in order to work around a bug where IntelliJ / Android Studio does not set the working directory to the module being tested. This can be accomplished by editing the run configurations, Defaults -> JUnit and changing the working directory value to $MODULE_DIR$.

> Source: [Robolectric Wiki](https://github.com/robolectric/robolectric/wiki/Running-tests-in-Android-Studio#notes-for-mac).

#### Android Studio Error: `SDK location not found. Define location with sdk.dir in the local.properties file or with an ANDROID_HOME environment variable.`
When cloning the project from Android Studio, click "No" when prompted to open the `build.gradle` file and then open project.

#### Moving to the main view if user minimizes the app
If you build the app on your own using Android Studio `(Build -> Build APK)` and then install it (from an `.apk` file), you might notice this strange behaviour thoroughly described: [#1280](https://github.com/opendatakit/collect/issues/1280) and [#1142](https://github.com/opendatakit/collect/issues/1142).

This problem occurs building other apps as well.

