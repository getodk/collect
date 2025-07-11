set -e

mkdir -p apks
rm -f apks/*

last_version_code=$1

git checkout $2
./gradlew clean
release_version_code=$((last_version_code + 1))
./gradlew assembleOdkCollectRelease -PversionCode=$release_version_code
cp collect_app/build/outputs/apk/odkCollectRelease/*.apk apks

if [[ $# -gt 2 ]]; then
    git checkout $3
    ./gradlew clean
    beta_version_code=$((release_version_code + 1))
    ./gradlew assembleOdkCollectRelease -PversionCode=$beta_version_code
    cp collect_app/build/outputs/apk/odkCollectRelease/*.apk apks
fi

open apks
