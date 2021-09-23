set -e

wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/6.0.1_r3-robolectric-r1/android-all-6.0.1_r3-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/7.0.0_r1-robolectric-r1/android-all-7.0.0_r1-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/11-robolectric-6757853/android-all-11-robolectric-6757853.jar -P robolectric-deps

mkdir -p collect_app/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p audiorecorder/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p projects/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p location/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p androidshared/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p geo/src/test/resources && cp robolectric-deps.properties "$_"
