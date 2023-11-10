set -e

wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/6.0.1_r3-robolectric-r1-i3/android-all-instrumented-6.0.1_r3-robolectric-r1-i3.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/6.0.1_r3-robolectric-r1-i4/android-all-instrumented-6.0.1_r3-robolectric-r1-i4.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/7.0.0_r1-robolectric-r1-i3/android-all-instrumented-7.0.0_r1-robolectric-r1-i3.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/7.0.0_r1-robolectric-r1-i4/android-all-instrumented-7.0.0_r1-robolectric-r1-i4.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/11-robolectric-6757853-i3/android-all-instrumented-11-robolectric-6757853-i3.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/12-robolectric-7732740-i3/android-all-instrumented-12-robolectric-7732740-i3.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/12-robolectric-7732740-i4/android-all-instrumented-12-robolectric-7732740-i4.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/12.1-robolectric-8229987-i4/android-all-instrumented-12.1-robolectric-8229987-i4.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/13-robolectric-9030017-i4/android-all-instrumented-13-robolectric-9030017-i4.jar -P robolectric-deps

mkdir -p collect_app/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p audiorecorder/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p projects/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p location/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p androidshared/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p geo/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p permissions/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p settings/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p maps/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p errors/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p selfie-camera/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p qr-code/src/test/resources && cp robolectric-deps.properties "$_"
mkdir -p draw/src/test/resources && cp robolectric-deps.properties "$_"
