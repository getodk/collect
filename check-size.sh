set -e

if [ $(ls -l collect_app/build/outputs/apk/selfSignedRelease/*.apk | awk '{print $5}') -gt 11800000 ];then
  echo "APK increased to $(ls -l collect_app/build/outputs/apk/selfSignedRelease/*.apk | awk '{print $5}') bytes!"
  exit 1
fi
