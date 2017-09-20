[Smap fieldTask](http://www.smap.com.au) 
[![Build Status](https://travis-ci.org/nap2000/fieldTask2.svg)](https://travis-ci.org/nap2000/fieldTask2)

<<<<<<< HEAD
fieldTask4 is a clone of [odkCollect](http://opendatakit.org/use/collect/) with Task Management functionality. It replaces fieldTask3 which used odkCollect as a library.
=======
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

**Google Maps API** - Follow the instructions [here](https://developers.google.com/maps/documentation/android-api/signup) and paste your key in the `AndroidManifest` as the value for `com.google.android.geo.API_KEY`. Please be sure not to commit your personal API key to a branch that you will submit a pull request for.
>>>>>>> v1.10.2
 
Follow the latest news about Smap on our [blog](http://blog.smap.com.au) and on twitter [@dgmsot](https://twitter.com/dgmsot).

Frequently Asked Questions
---------------------------
##### How to install and run
* Install Android Studio
* In Android Studio open the SDK manager (from the tools menu)
* Under "Extras" install:
    * Android Support Repository
    * Android Support Library
    * Google Play Services
    * Google Repository
* Clone as a GIT project into Android Studio
* Checkout branch "smap"
* Select fieldTask and run as an Android application

Instructions on installing a Smap server can be found in the operations manual [here](http://www.smap.com.au/downloads.shtml)

Task Management 
---------------

A user of fieldTask can be assigned tasks to complete as per this [video](http://www.smap.com.au/taskManagement.shtml). 

##### Get existing survey data as an XForm instance XML file
https://hostname/instanceXML/{survey id}/0?datakey={key name}&datakeyvalue={value of key}

##### Update existing results
https://{hostname}/submission/{instanceid}

Note the instance id of the existing data is included in the instanceXML.  It should be replaced with a new instance id before the results are submitted. However the instance id of the data to be replaced needs to be included in teh submission URL.

This API allows you to maintain data using surveys. In the following video the data is published on a map, however it could also be published in a table as a patient registry or list of assets. fieldTask needs to be customised to access these links using the data keys in a similar way to web forms.

[![ScreenShot](http://img.youtube.com/vi/FUNPOmMnt1I/0.jpg)](https://www.youtube.com/watch?v=FUNPOmMnt1I)

Development
-----------
* Code contributions are very welcome. 
* [Issue Tracker](https://github.com/smap-consulting/fieldTask4/issues)

Acknowledgements
----------------

This project includes:
* the odkCollect Library of (http://opendatakit.org/) from the University of Washington
* the Android SDK from [MapBox] (https://www.mapbox.com/)
