package org.odk.collect.android.utilities;

import android.content.Context;

public final class ViewUtils {

    private ViewUtils() {
        
    }

    public static int dpFromPx(final Context context, final float px) {
        return Math.round(px / context.getResources().getDisplayMetrics().density);
    }

    public static int pxFromDp(final Context context, final float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

}
