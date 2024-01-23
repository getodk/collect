package org.odk.collect.androidshared.system

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

object ContextUtils {

    /**
     * Be careful when using this method to retrieve colors, especially for those defined
     * using selectors as it might not work well.
     * In such cases consider using [com.google.android.material.color.MaterialColors.getColor] instead.
     */
    @JvmStatic
    fun getThemeAttributeValue(context: Context, @AttrRes resId: Int): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(resId, outValue, true)
        return outValue.data
    }
}
