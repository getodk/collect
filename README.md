# Field Task

Field Task is an Android app that can be used for collecting data for Monitoring and Evaluation from outside the office including locations without data connectivity. It also has task management functionality which enables it to be used for work force management applications.

Field Task is a fork of [odkCollect](http://opendatakit.org/use/collect/). 
 

## Table of Contents
* [Dependencies](#dependencies)
* [Setting up your development environment](#setting-up-your-development-environment)
* [Branches](#branches)
* [Incorporating the latest ODK release](#incorporating-the-latest-odk-release)

## Dependencies
Most dependencies are managed using gradle, however some Field Task functionality requires a modified version of JavaRosa(https://github.com/smap-consulting/javarosa).  You will need to download this to your development environment in order to access these features.

## Setting up your development environment

1. Download and install [Git](https://git-scm.com/downloads) and add it to your PATH

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) 

1. Fork the Field Task project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/fieldTask4

1. Use Android Studio to import the project from its Gradle settings. To run the project, click on the green arrow at the top of the screen.

1. Windows developers: continue configuring Android Studio with the steps in this document: [Developing ODK Collect on Windows](docs/WindowsDevSetup.md).

1. Make sure you can run unit tests by running everything under `collect_app/src/test/java` in Android Studio or on the command line:

    ```
    ./gradlew testDebug
    ```

1. Make sure you can run instrumented tests by running everything under `collect_app/src/androidTest/java` in Android Studio or on the command line:

    ```
    ./gradlew connectedAndroidTest
    ```
## Branches
* production - The latest Field Task code
* master - The latest unmodifield code from ODK

## Incorporating the latest ODK release

1. Update the master branch to the latest version. On the commnad line

        git checkout master
        git fetch upstream
        git merge upstream/master
        
1. Create a branch with the version you want to merge into Field Task

        git branch merge_master <tag name>
        
   <tag name> is the tag in odk collect identifying the version that you want.  merge_master is the temporary branch that will be created. You can name it as you wish.
 
 1. Create a branch in which to merge the code

        git checkout production
        git checkout -b merge
        git merge --no-commit merge_master
        
    This creates a temporary branch called merge from the latest production branch and then merges code from merge_master without committing the changes
 

Acknowledgements
----------------

This project includes:
* the odkCollect Library of (http://opendatakit.org/)
* the Android SDK from [MapBox] (https://www.mapbox.com/)
