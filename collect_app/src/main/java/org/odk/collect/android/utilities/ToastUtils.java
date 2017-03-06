package org.odk.collect.android.utilities;

import android.content.res.Resources;
import android.widget.Toast;

import org.odk.collect.android.application.Collect;


public class ToastUtils {

    private static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    private static final int LENGTH_LONG = Toast.LENGTH_LONG;


    public static void showShortToast(String message) {
        Toast(message, LENGTH_SHORT);
    }

    public static void showShortToast(int messageResource) {
        Toast(messageResource, LENGTH_SHORT);
    }

    public static void showLongToast(String message) {
        Toast(message, LENGTH_LONG);
    }

    public static void showLongToast(int messageResource) {
        Toast(messageResource, LENGTH_LONG);
    }

    private static void Toast(String message, int duration) {
        Toast.makeText(Collect.getInstance(), message, duration).show();
    }

    private static void Toast(int messageResource, int duration) {
        try {
            Toast.makeText(Collect.getInstance(), Collect.getInstance().getString(messageResource), duration).show();
        } catch (Resources.NotFoundException nfe) {
            nfe.printStackTrace();
        }
    }
}
