# Developing and Testing ODK Collect Using Containers

## Overview
Typically ODK devs would install Android Studio to compile, test, and emulate Collect during development.

However, not all testers are necessarily Android developers, and some may wish for a means to test without installing many additional dependencies.

In this case, it is possible to build and test Collect entirely inside containers.

## Test the code
* First check `.circleci/config.yml:references.android_config.docker.image` for changes to the image used below.

* Run the CI image used to compile and test:
  ```bash
  docker run --rm -it -u 0:0 -v $PWD:$PWD -v $PWD/.circleci/gradle.properties:$PWD/.gradle/gradle.properties --workdir=$PWD cimg/android:2023.10.1 bash
  ```
  * This will run the container as root to avoid permission errors.
  * Be aware that any new files will be created as root.

* To avoid issue with Git, mark the repo as safe:
  `git config --global --add safe.directory $PWD`

* Download extra testing dependencies:
  `./download-robolectric-deps.sh`

* Compile the code:
  `./gradlew assembleDebug`

* Code quality checks:
  `./gradlew pmd ktlintCheck checkstyle lintDebug`

* Run unit tests:
  `./gradlew testDebug`

* Run instrumented tests:
  `./gradlew connectedAndroidTest`

## Compile the APK
* Assmble the test build:
  `./gradlew assembleDebugAndroidTest`
  * The output APK is located: `collect_app/build/outputs/apk/debug/collect-debug-null.apk`.
  * Follow the steps in the [next section](#run-an-android-emulator).
* Assmble the self-signed APK:
  `./gradlew assembleDebugAndroidTest`
  * The output APK is located: `collect_app/build/outputs/apk/selfSignedRelease/*.apk`.
  * Code the APK to your Android device and sideload for testing.

## Run an Android emulator
* To run the debug APK, we must first start an Android emulator.

  **Note**: currently this method only works on Linux.

* Check that virtualization is enabled on your machine:
  `sudo apt update && sudo apt install -y cpu-checker && kvm-ok`

* Run the emulator, with the repo mounted:
  ```bash
  docker run -d -v $PWD:$PWD --workdir=$PWD -p 6080:6080 -e DEVICE="Samsung Galaxy S10" -e WEB_VNC=true --device /dev/kvm --name android-container budtmo/docker-android:emulator_14.0
  ```

* Enter the running container terminal:
  `docker exec -it android-container bash`

* Install the APK to the emulated device:
  ```bash
  adb -e install collect_app/build/outputs/apk/debug/collect-debug-null.apk
  ```

* Load the emulated device in your web browser to use Collect: [http://localhost:6080/](http://localhost:6080/)

* To load in additional files, you may need to push via ADB:
  `adb -e push /path/to/file.ext /storage/emulated/0/Download/file.ext`
