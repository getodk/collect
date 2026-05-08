package org.odk.collect.entities.javarosa.parse

import android.net.Uri
import androidx.core.net.toUri
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun String?.isV4UUID(): Boolean {
    contract {
        returns(true) implies (this@isV4UUID != null)
    }

    return if (this != null) {
        try {
            UUID.fromString(this).version() == 4
        } catch (_: IllegalArgumentException) {
            false
        }
    } else {
        false
    }
}

fun String.toUri(vararg params: Pair<String, String?>): Uri =
    this.toUri()
        .buildUpon()
        .apply {
            params.forEach { (key, value) ->
                appendQueryParameter(key, value)
            }
        }
        .build()
