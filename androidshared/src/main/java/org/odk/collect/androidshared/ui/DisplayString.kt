package org.odk.collect.androidshared.ui

import android.content.Context

sealed class DisplayString {

    data class Raw(val value: String) : DisplayString()
    data class Resource(val resource: Int) : DisplayString()

    fun getString(context: Context): String {
        return when (this) {
            is Raw -> value
            is Resource -> context.getString(resource)
        }
    }
}
