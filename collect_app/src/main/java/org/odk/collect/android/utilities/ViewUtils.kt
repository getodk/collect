package org.odk.collect.android.utilities

import android.content.Context
import kotlin.math.roundToInt

object ViewUtils {
    @JvmStatic
    fun dpFromPx(context: Context, px: Float): Int {
        return (px / context.resources.displayMetrics.density).roundToInt()
    }

    @JvmStatic
    fun pxFromDp(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).roundToInt()
    }
}
