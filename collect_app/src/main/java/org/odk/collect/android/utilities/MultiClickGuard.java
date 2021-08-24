package org.odk.collect.android.utilities;

import android.os.SystemClock;

public final class MultiClickGuard {
    private static final int CLICK_DEBOUNCE_MS = 1000;
    public static boolean test;

    private static long lastClickTime;
    private static String lastClickName;

    private MultiClickGuard() {
    }

    // Debounce multiple clicks within the same screen
    public static boolean allowClick(String className) {
        if (test) {
            return true;
        }

        long elapsedRealtime = SystemClock.elapsedRealtime();
        boolean isSameClass = className.equals(lastClickName);
        boolean isBeyondThreshold = elapsedRealtime - lastClickTime > CLICK_DEBOUNCE_MS;
        boolean isBeyondTestThreshold = lastClickTime == 0 || lastClickTime == elapsedRealtime; // just for tests
        boolean allowClick = !isSameClass || isBeyondThreshold || isBeyondTestThreshold;
        if (allowClick) {
            lastClickTime = elapsedRealtime;
            lastClickName = className;
        }
        return allowClick;
    }
}
