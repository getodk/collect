set -e

[ "$1" = "-h" -o "$1" = "--help" ] && echo "
    Usage: `basename $0` lastReleasedVersionCode nextReleaseTag [additionalReleaseTag]

    Creates up to two releases with versionCodes incremented from lastReleasedVersionCode. The last
    released version code will typically be the last version code published on the Play Store (beta
    or production). Two releases are needed when a patch is needed to the last production release
    and a beta is already ongoing for the next release.
" && exit

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
