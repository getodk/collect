# Field Task

Field Task is an Android app that can be used for collecting data for Monitoring and Evaluation from outside the office including locations without data connectivity. It also has task management functionality which enables it to be used for work force management applications.  Further details on installing and testing Field Task on an Android device can be found [here](https://www.smap.com.au/docs/fieldTask.html).  You will need to [set up an account](https://www.smap.com.au/docs/getting-started.html#create-an-account-on-the-hosted-server) on the free hosted server to test the app.
 

## Table of Contents
* [Dependencies](#dependencies)
* [Setting up your development environment](#setting-up-your-development-environment)
* [Branches](#branches)
* [Incorporating the latest upstream changes](#incorporating-the-latest-upstream-changes)
* [Changes in the Field Task fork](#changes-in-the-field-task-fork)
* [Acknowledgements](#acknowledgements)

## Dependencies
Most dependencies are managed using gradle, however some Field Task functionality requires a modified version of JavaRosa(https://github.com/smap-consulting/javarosa).  You will need to add this to your development environment in order to access these features.

## Setting up your development environment

1. Download and install [Git](https://git-scm.com/downloads) if you don't have it already

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) if you don't have it already

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

1. Select run

## Branches
* production - The latest Field Task code
* master - The latest unmodifield code from the upstream repository

## Incorporating the latest upstream changes

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
    
1. Fix merge issues.  

    There are likely to be many of these.  Smap changes from ODK are either in their own files or marked with the comment "smap".  If there is a difference and no "smap" commentt then you can generally accept the version from ODK.  Otherwise a manual merge is required to preserve the smap functionality.
 
## Changes in the Field Task fork

The following changes from the upstream implementation will need to be merged and then tested before releasing a new version that includes upstream updates/

*  Login Page.   
*  One touch synchronisation
*  Form List, Task List and Map Tabs on the home page.
*  Automatic synchronisation when a form is changed on the server.
*  Geofencing for showing and downloading of forms
*  Chart Widget
*  Form Launcher Widget
*  NFC Widget
*  Auto launching camera, barcode and other widgets
*  Task Management
*  Self Assigned Tasks
*  Navigation to tasks using google maps
*  Online lookup of choice values
*  Online pulldata
*  Online lookup of images for annotation
*  Online lookup of labels in images
*  Searching for choice values using "in" and "not in" functions
*  pulldata acting on multiple records

Acknowledgements
----------------

This project:
* forks the odkCollect Library of (http://opendatakit.org/)
* includes the Android SDK from [MapBox] (https://www.mapbox.com/)
