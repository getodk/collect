# collect

This project is __*actively maintained*__

It is the ODK 1.0 Android application.

The developer [wiki](https://github.com/opendatakit/opendatakit/wiki) (including release notes) and
[issues tracker](https://github.com/opendatakit/opendatakit/issues) are located under
the [**opendatakit**](https://github.com/opendatakit/opendatakit) project.

The Google group for software engineering questions is: [opendatakit-developers@](https://groups.google.com/forum/#!forum/opendatakit-developers)

## Setting up your environment

This project depends upon the gradle-config project. The necessary gradle files will be downloaded and used automatically from the github repository at the tag specified in settings.gradle. If you wish to modify your gradle-config yourself, you must clone the project into the same parent directory as Collect. Your directory structure should resemble the following: 

        |-- odk
            |-- gradle-config
            |-- collect

The `gradle-config` project should be checked out at the tag number declared at the 
top of the `collect/settings.gradle` file.

Then, import the `collect/build.gradle` file into Android Studio.

