package org.odk.collect.androidshared.system

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

object ContextUtils {

    @JvmStatic
    fun getThemeAttributeValue(context: Context, @AttrRes resId: Int): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(resId, outValue, true)
        return outValue.data
    }
}
