package org.odk.collect.android.utilities;

import android.widget.Toast;

import org.odk.collect.android.application.Collect;


public class ToastUtils {
    public static void makeShortText(String message) {
        toaster(message, Toast.LENGTH_SHORT);
    }

    public static void makeLongText(String message) {
        toaster(message, Toast.LENGTH_LONG);
    }

    private static void toaster(String message, int duration) {
        Toast.makeText(Collect.getInstance(), message, duration).show();
    }
}
