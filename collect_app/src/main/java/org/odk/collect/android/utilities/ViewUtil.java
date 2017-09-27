package org.odk.collect.android.utilities;


import android.os.Build;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

public class ViewUtil {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        } else {
            return generateViewIdPre17();
        }
    }

    private static int generateViewIdPre17() {
        while (true) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    private ViewUtil() {
    }
}