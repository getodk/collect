package org.odk.collect.android.utilities;

public class ImageUtils {

    // http://code.google.com/p/android/issues/detail?id=1480
    public static boolean hasImageCaptureBug() {

        String brand = android.os.Build.BRAND.toLowerCase();
        String device = android.os.Build.DEVICE.toLowerCase();
        String board = android.os.Build.BOARD.toLowerCase();
        
        if ((brand.contains("google") || brand.contains("android"))
                || (brand.equals("generic") && device.equals("generic") && board.equals("unknown"))) {
            return true;
        }
        return false;
    }
}
