package org.odk.collect.settings.enums

import android.content.Context
import androidx.annotation.StringRes
import org.odk.collect.settings.R

enum class AutoSend(@StringRes private val value: Int) {

    OFF(R.string.auto_send_off),
    WIFI_ONLY(R.string.auto_send_wifi_only),
    CELLULAR_ONLY(R.string.auto_send_cellular_only),
    WIFI_AND_CELLULAR(R.string.auto_send_wifi_and_cellular);

    fun getValue(context: Context): String {
        return context.getString(value)
    }

    companion object {

        @JvmStatic
        fun parse(context: Context, value: String?): AutoSend {
            return entries.find {
                context.getString(it.value) == value
            } ?: throw IllegalArgumentException()
        }
    }
}
