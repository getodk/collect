package org.odk.collect.android.utilities;

import android.widget.Toast;

import org.odk.collect.android.application.Collect;


public class ToastUtils {

    private static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    private static final int LENGTH_LONG = Toast.LENGTH_LONG;


    public static void showShortToast(String message) {
        toaster(message, LENGTH_SHORT);
    }

    public static void showLongToast(String message) {
        toaster(message, LENGTH_LONG);
    }

    public static void showShortToast(int messageResource) {
        toaster(messageResource, LENGTH_SHORT);
    }

    public static void showLongToast(int messageResource) {
        toaster(messageResource, LENGTH_LONG);
    }

    private static void toaster(int messageResource, int duration) {
        Toast.makeText(Collect.getInstance(), Collect.getInstance().getString(messageResource), duration).show();
    }

    private static void toaster(String message, int duration) {
        Toast.makeText(Collect.getInstance(), message, duration).show();
    }
}
