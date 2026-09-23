package org.odk.collect.crashhandler

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName

// @SerializedName on every field keeps the JSON keys stable when obfuscation renames the fields
internal data class SerializedCrash(
    @SerializedName("outOfMemory") val outOfMemory: Boolean,
    @SerializedName("message") val message: String
) {
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
