package org.odk.collect.settings.enums

import android.content.Context

internal interface StringIdEnum {
    val stringId: Int

    fun getValue(context: Context): String {
        return context.getString(stringId)
    }
}
