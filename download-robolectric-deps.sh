set -e

wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/6.0.1_r3-robolectric-r1-i7/android-all-instrumented-6.0.1_r3-robolectric-r1-i7.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/7.0.0_r1-robolectric-r1-i7/android-all-instrumented-7.0.0_r1-robolectric-r1-i7.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/12.1-robolectric-8229987-i7/android-all-instrumented-12.1-robolectric-8229987-i7.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/13-robolectric-9030017-i7/android-all-instrumented-13-robolectric-9030017-i7.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/15-robolectric-12650502-i7/android-all-instrumented-15-robolectric-12650502-i7.jar -P robolectric-deps

dest_dir="src/test/resources"
mkdir -p collect_app/$dest_dir && cp robolectric-deps.properties collect_app/$dest_dir
mkdir -p audiorecorder/$dest_dir && cp robolectric-deps.properties audiorecorder/$dest_dir
mkdir -p projects/$dest_dir && cp robolectric-deps.properties projects/$dest_dir
mkdir -p location/$dest_dir && cp robolectric-deps.properties location/$dest_dir
mkdir -p androidshared/$dest_dir && cp robolectric-deps.properties androidshared/$dest_dir
mkdir -p geo/$dest_dir && cp robolectric-deps.properties geo/$dest_dir
mkdir -p permissions/$dest_dir && cp robolectric-deps.properties permissions/$dest_dir
mkdir -p settings/$dest_dir && cp robolectric-deps.properties settings/$dest_dir
mkdir -p maps/$dest_dir && cp robolectric-deps.properties maps/$dest_dir
mkdir -p errors/$dest_dir && cp robolectric-deps.properties errors/$dest_dir
mkdir -p selfie-camera/$dest_dir && cp robolectric-deps.properties selfie-camera/$dest_dir
mkdir -p qr-code/$dest_dir && cp robolectric-deps.properties qr-code/$dest_dir
mkdir -p draw/$dest_dir && cp robolectric-deps.properties draw/$dest_dir
