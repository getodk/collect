package org.odk.collect.android.utilities;

import android.view.View;

public class ViewIds {

    /**
     * Generates a unique integer ID for a View.
     * Falls back to the system provided {@link View#generateViewId()} when possible.
     *
     * @return A unique integer ID.
     */
    public static int generateViewId() {
        return View.generateViewId();
    }

    private ViewIds() {}
}