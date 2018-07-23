package org.odk.collect.android.utilities;


import android.os.Build;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

public class ViewIds {

    private static final AtomicInteger NEXT_GENERATED_ID = new AtomicInteger(1);

    /**
     * Generates a unique integer ID for a View.
     * Falls back to the system provided {@link View#generateViewId()} when possible.
     *
     * @return A unique integer ID.
     */
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        } else {
            return generateViewIdPre17();
        }
    }

    /**
     * This is a line-for-line copy of the built-in, post API 17 {@link View#generateViewId()}.
     * A bit odd, but what the system uses internally for generating unique IDs.
     *
     * @return A unique integer ID to be used for a View.
     */
    private static int generateViewIdPre17() {
        while (true) {
            final int result = NEXT_GENERATED_ID.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) {
                newValue = 1; // Roll over to 1, not 0.
            }

            if (NEXT_GENERATED_ID.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    private ViewIds() {}
}