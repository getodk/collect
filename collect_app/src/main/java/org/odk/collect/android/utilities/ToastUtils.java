package org.odk.collect.android.utilities;

import android.widget.Toast;

import org.odk.collect.android.application.Collect;


public class ToastUtils {

    private static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    private static final int LENGTH_LONG = Toast.LENGTH_LONG;


    public static void showShortToast(String message) {
        showToast(message, LENGTH_SHORT);
    }

    public static void showShortToast(int messageResource) {
        showToast(messageResource, LENGTH_SHORT);
    }

    public static void showLongToast(String message) {
        showToast(message, LENGTH_LONG);
    }

    public static void showLongToast(int messageResource) {
        showToast(messageResource, LENGTH_LONG);
    }

    private static void showToast(String message, int duration) {
        Toast.makeText(Collect.getInstance(), message, duration).show();
    }

    private static void showToast(int messageResource, int duration) {
        Toast.makeText(Collect.getInstance(), Collect.getInstance().getString(messageResource), duration).show();
    }
}
