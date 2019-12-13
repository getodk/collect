# Developing ODK Collect on Windows

## Overview
Most ODK devs are using OS X or Linux. Occasionally problems surface for Windows users due to code changes or tool changes. Such problems commonly include file path separators and file permissions differences from Linux/OSX. However in general it is possible to develop ODK using Android Studio on Windows.

## Known Issues
* Do not upgrade to Android Studio 3.6. See: [Issue 3507](https://github.com/opendatakit/collect/issues/3507)
* Firebase Perf plugin exposes bug in Android Studio 3.5: <https://stackoverflow.com/a/59015733>

## Current Workarounds
* Firebase Perf: disable with the following modifications

 __build.gradle__:  comment out the line that looks like:

        classpath 'com.google.firebase:perf-plugin:1.3.1'

 __collect_app/build.gradle__: comment out the two firebase-perf lines that look like:

        apply plugin: 'com.google.firebase.firebase-perf'
        implementation 'com.google.firebase:firebase-perf:19.0.2' 
    
## Configuring Android Studio for Windows
* Change line endings to Unix. Settings > Editor > Code Style > General (tab) > Line Separator :: Unix and Mac OS (\n)
* Configure the Terminal environment settings for Java:
  Settings > Tools > Terminal
  `JAVA_HOME=C:/Program Files/Android/Android Studio/jre`
* Accept SDK licenses if necessary. Run from the Terminal in Android Studio using your own username AppData path:
  `"C:\Users\<username>\AppData\Local\Android\Sdk\tools\bin\sdkmanager.bat" --licenses`
* To be able to run / debug tests using Android Studio, edit the JUnit configuration to use a classpath file instead of a bloated classpath string (which maxes out the tiny Windows limit for environment variables):
  Run > Edit Configurations... > (left pane)Templates > Android JUnit > (right pane)Shorten command line: *classpath file*


## Configuring Git for Windows
* To work around file permissions differences on Windows that appear as errant file edits, tell Git to ignore permissions differences by changing the git config:
  `core.filemode = false`
* More info: <https://community.atlassian.com/t5/Sourcetree-questions/Why-is-sourceTree-showing-changes-when-there-are-none/qaq-p/186916>