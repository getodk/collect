set -e

wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/6.0.1_r3-robolectric-r1/android-all-6.0.1_r3-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/7.0.0_r1-robolectric-r1/android-all-7.0.0_r1-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/9-robolectric-4913185-2/android-all-9-robolectric-4913185-2.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/10-robolectric-5803371/android-all-10-robolectric-5803371.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/11-robolectric-6757853/android-all-11-robolectric-6757853.jar -P robolectric-deps

cp robolectric-deps.properties collect_app/src/test/resources
cp robolectric-deps.properties audiorecorder/src/test/resources
cp robolectric-deps.properties projects/src/test/resources
cp robolectric-deps.properties location/src/test/resources
