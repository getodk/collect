package org.odk.collect.android.utilities;

public class ImageUtils {

    // http://code.google.com/p/android/issues/detail?id=1480
    public static boolean hasImageCaptureBug() {

        String brand = android.os.Build.BRAND.toLowerCase();

        if (brand.contains("google") || brand.contains("android") || brand.contains("generic")) {
            return true;
        }

        return false;
    }

}
