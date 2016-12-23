# ODK Collect
![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/opendatakit/collect.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/opendatakit/collect)
[![Slack status](http://slack.opendatakit.org/badge.svg)](http://slack.opendatakit.org)

ODK Collect is an Android app for filling out forms. It is designed to be used in resource-constrained environments with challenges such as unreliable connectivity or power infrastructure. ODK Collect is part of Open Data Kit (ODK), a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the Open Data Kit project and its history [here](https://opendatakit.org/about/) and read about example ODK deployments [here](https://opendatakit.org/about/deployments/).

ODK Collect renders forms that are compliant with the [ODK XForms standard](http://opendatakit.github.io/odk-xform-spec/), a subset of the [XForms 1.1 standard](https://www.w3.org/TR/xforms/) with some extensions. The form parsing is done by the [JavaRosa library](https://github.com/opendatakit/javarosa) which Collect includes as a jar.

* ODK website: [https://opendatakit.org](https://opendatakit.org)
* ODK Collect usage instructions: [https://opendatakit.org/use/collect](https://opendatakit.org/use/collect/)
* ODK community mailing list: [http://groups.google.com/group/opendatakit](http://groups.google.com/group/opendatakit)
* ODK developer mailing list: [http://groups.google.com/group/opendatakit-developers](http://groups.google.com/group/opendatakit-developers)
* ODK developer Slack chat: [http://slack.opendatakit.org](http://slack.opendatakit.org) 
* ODK developer Slack archive: [http://opendatakit.slackarchive.io](http://opendatakit.slackarchive.io) 
* ODK developer wiki: [https://github.com/opendatakit/opendatakit/wiki](https://github.com/opendatakit/opendatakit/wiki)

## Setting up your development environment

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) 

1. Fork the collect project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/collect

 If you prefer not to use the command line, you can use Android Studio to create a new project from version control using `https://github.com/YOUR-GITHUB-USERNAME/collect`. 

1. Open the project in the folder of your clone from Android Studio. To run the project, click on the green arrow at the top of the screen. The emulator is very slow so we generally recommend using a physical device when possible.
 
## Contributing code
Any and all contributions to the project are welcome. ODK Collect is used across the world primarily by organizations with a social purpose so you can have real impact!

Issues tagged as [quick win](https://github.com/opendatakit/collect/labels/quick%20win) should be a good place to start. There are also currently many issues tagged as [needs reproduction](https://github.com/opendatakit/collect/labels/needs%20reproduction) which need someone to try to reproduce them with the current version of ODK Collect and comment on the issue with their findings.

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

## Contributing in other ways
If you know a language other than English, consider contributing translations through [Transifex](https://www.transifex.com/opendatakit/odk-collect/).

You can also help by improving this documentation.

## Downloading builds
Per-commit debug builds can be found on [CircleCI](https://circleci.com/gh/opendatakit/collect). Login with your GitHub account, click the build you'd like, then find the APK in the Artifacts tab.

Current and previous production builds can be found on the [ODK website](https://opendatakit.org/downloads/download-info/odk-collect-apk).

## Troubleshooting
#### Android Studio Error: `SDK location not found. Define location with sdk.dir in the local.properties file or with an ANDROID_HOME environment variable.`
When cloning the project from Android Studio, click "No" when prompted to open the `build.gradle` file and then open project.
