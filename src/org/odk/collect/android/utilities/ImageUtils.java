package org.odk.collect.android.utilities;

public class ImageUtils {

    // http://code.google.com/p/android/issues/detail?id=1480
    public static boolean hasImageCaptureBug() {

        String brand = android.os.Build.BRAND.toLowerCase();
        String version = android.os.Build.VERSION.INCREMENTAL.toLowerCase();
        if (brand.contains("google") || brand.contains("android")) {
            return true;
        } else if (brand.contains("generic")) {
            if (version.equals("eng.u70000.20090527.151446")) {
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

}
