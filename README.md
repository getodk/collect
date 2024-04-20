# Field Task

Field Task is an Android app that can be used for collecting data for Monitoring and Evaluation from outside the office including locations without data connectivity. It is part of the Smap framework for M&E and Case Management which means it can be used for digitilisation projects.  Further details on installing and testing Field Task on an Android device can be found [here](https://www.smap.com.au/docs/fieldTask.html).  You will need to [set up an account](https://www.smap.com.au/docs/getting-started.html#create-an-account-on-the-hosted-server) on the free hosted server to test the app.
 

## Table of Contents
* [Dependencies](#dependencies)
* [Setting up your development environment](#setting-up-your-development-environment)
* [Branches](#branches)
* [Variants](#variants)
* [Incorporating the latest upstream changes](#incorporating-the-latest-upstream-changes)
* [Changes in the Field Task fork](#changes-in-the-field-task-fork)
* [Acknowledgements](#acknowledgements)

## Dependencies
Most dependencies are managed using gradle, however some Field Task functionality requires a modified version of JavaRosa(https://github.com/smap-consulting/javarosa).  You will need to add this to your development environment in order to access these features.

## Setting up your development environment

1. Download and install [Git](https://git-scm.com/downloads) if you don't have it already

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) if you don't have it already

1. In Android Studio install the target SDK

1. [Add Smap JavaRosa as a project in Android Studio](https://github.com/smap-consulting/javarosa)

        Follow the readme instructions in the javarosa repository to build the latest production java rosa version. Currently tip of production branch.
        copy the generated jar file to fieldTask/collect_app/libs. You may need to create the libs folder
        The version of java rosa that you need is referenced in the application's build.gradle

1. Fork the Field Task project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line type "git clone" and then the url provided by git hub.

        git clone https://github.com/YOUR-GITHUB-USERNAME/fieldTask4.git
        
1. Checkout the production branch

        git checkout production

1. Use Android Studio to import the project

1. Get a Google services json file

        Sign into the firebase console
        Add a project
        Select Android to add firebase to your Android app
        Specify the package as: org.smap.smapTask.android
        Register the app 
        Download the google-services.json file and place it in the fieldTas4/collect_app folder

1. [Get a Google Maps API key](https://developers.google.com/maps/documentation/android-api/signup).  A credit card number is required however there a fairly significant number of requests available at no charge each month.  Check how many map calls you expect to be using and the expected cost before signing up.

1. Enable Google Maps for your application.  Your apk needs to be given acces to the google maps API key that you have created.

       Goto the google developers console
       Click enable APIs and services
       Select Maps SDK for Android
       Select Manage
       Select Credentials
       Add the package name and sha-1 signing certificate for your app to the google maps API key that you created in the previous step

1. [Create a Mapbox account](https://www.mapbox.com/signup/).  Select the "Pay-As-You-Go" plan.  Like Google, Mapbox provides free API usage up to the monthly thresholds documented at [https://www.mapbox.com/pricing](https://www.mapbox.com/pricing).  You can find your access token on your [account page](https://account.mapbox.com/).
 
1. Edit or create `collect_app/secrets.properties` and set the `GOOGLE_MAPS_API_KEY` and the `MAPBOX_ACCESS_TOKEN` property to your access keys.  You should end up with two    lines that looks like this:
   ```
    GOOGLE_MAPS_API_KEY=AIzaSyA9C_...
    MAPBOX_ACCESS_TOKEN=pk.eyJ1IjoibmFwMjAwMCIsImEiOiJja...
   ```
1. Select run to view fieldTask in an emulator

## Branches
* production - The latest Field Task code

## Variants
The variant in the github repository that you will want to start with is "standard".  This contains some resources and Java code to handle customisable processing around location change.  Background location recording can be enabled on FieldTask and by default this is used only for geo fencing and the location data is not stored.   The register function in the LocationRegister file can be modified to do additional processing of location changes if you need that. 

Related Repositories
--------------------

FieldTask is designed to work with Smap server, the code for which is included in other GitHub repositories.  The complete list of repositories is shown 
in the table below.

|Name          |Github                                        |Purpose    |
|------------- |--------------------------------------------- |-----------|
|FieldTask     |https://github.com/smap-consulting/fieldTask4 |FieldTask  |
|JavaRosa      |https://github.com/smap-consulting/javarosa   |FieldTask  |
|SmapServer    |https://github.com/smap-consulting/smapserver2|Back end Server code     |
|WebForm       |https://github.com/nap2000/enketo-core        |Browser client    |
|SmapClient    |https://github.com/nap2000/prop-smapserver    |Administrative client     |
|Documentation |https://github.com/nap2000/docs               |Documentation |

Downloads
---------

|Name          |Link                                                          |Version    |
|------------- |------------------------------------------------------------- |-----------|
|Server        |https://smap-master.s3.amazonaws.com/smap_24_03_18_760.tgz    |23.03.18   |

[Server Administration Documentation](https://www.smap.com.au/docs/server-admin.html)

Acknowledgements
----------------

This project:
* includes the Android SDK from [MapBox] (https://www.mapbox.com/)
* forks the odkCollect Library of (http://opendatakit.org/)
