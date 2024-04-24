package org.odk.collect.settings.enums

import android.content.Context
import androidx.annotation.StringRes
import org.odk.collect.settings.R

enum class AutoSend(@StringRes override val stringId: Int) : StringIdEnum {
    OFF(R.string.auto_send_off),
    WIFI_ONLY(R.string.auto_send_wifi_only),
    CELLULAR_ONLY(R.string.auto_send_cellular_only),
    WIFI_AND_CELLULAR(R.string.auto_send_wifi_and_cellular)
}

interface StringIdEnum {
    val stringId: Int

    fun getValue(context: Context): String {
        return context.getString(stringId)
    }
}
