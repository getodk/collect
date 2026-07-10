package org.odk.collect.crashhandler

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

internal data class SerializedCrash(val outOfMemory: Boolean, val message: String) {
    fun encode(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun decode(string: String?): SerializedCrash? {
            return if (string != null) {
                try {
                    Gson().fromJson(string, SerializedCrash::class.java)
                } catch (_: JsonSyntaxException) {
                    null
                }
            } else {
                null
            }
        }
    }
}
